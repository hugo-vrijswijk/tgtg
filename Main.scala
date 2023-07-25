package tgtg

import cats.effect.{ExitCode, IO, Resource}
import cats.syntax.all.*
import com.monovore.decline.Opts
import com.monovore.decline.effect.CommandIOApp
import fs2.Stream
import org.legogroup.woof.{Logger, given}
import tgtg.notification.Notification

import scala.concurrent.duration.*

val version = sys.env
  .get("GITHUB_REF")
  .collect:
    case s"refs/tags/$tag" => tag
  .getOrElse("dev")

object Main extends CommandIOApp("tgtg", "TooGoodToGo notifier for your favourite stores", version = version):

  override def main: Opts[IO[ExitCode]] = (Config.opts orElse Config.auth).map: config =>
    Deps
      .mkLogger(config.log)
      .flatMap: log =>
        given Logger[IO] = log

        config match
          case config: AuthConfig =>
            Deps
              .mkHttpBackend(none)
              .use: http =>
                val tgtg = TooGoodToGo(http)
                val auth = new Auth(tgtg, config)
                auth.run
                  .as(ExitCode.Success)
                  .handleErrorWithLog

          case config: Config =>
            val main = new Main(config)
            config.server
              .fold(main.run)(main.loop(_).guarantee(log.info("Shutting down") *> log.info("Bye!")))
              .handleErrorWithLog
              .as(ExitCode.Success)
        end match

end Main

final class Main(config: Config)(using log: Logger[IO]):

  /** Run the main loop (log errors, never exit)
    */
  def loop(server: ServerConfig): IO[Unit] = fs2.Stream
    .repeatEval(run.handleErrorWithLog)
    .evalTap(_ => log.info(show"Sleeping for ${server.interval}"))
    .meteredStartImmediately(server.interval)
    .compile
    .drain

  /** Run once
    */
  def run =
    (Deps.mkHttpBackend(config.cronitor), Deps.mkCache(config.redis)).parTupled.use: (http, cache) =>

      val tgtg   = TooGoodToGo(http)
      val notify = Deps.mkNotifyService(http, config.notification)

      tgtg
        .getItems(cache, config.tgtg)
        .map(_.filter(_.items_available > 0))
        .flatMap: items =>
          if items.isEmpty then log.info("No boxes to notify for.")
          else
            items.parTraverse_ : item =>
              val key = show"gotify-${item.store.store_name}"

              val notificationTimeout = 60.minutes

              cache
                .get[Boolean](key)
                .attempt
                .map(_.toOption.flatten)
                .map(!_.contains(true))
                .ifM(
                  cache
                    .set(true, key, notificationTimeout)
                    .guarantee(
                      notify.sendNotification(Notification(title = item.display_name, message = item.show))
                    ),
                  log.info(
                    show"Found boxes, but notification for '$key' was already sent in the last $notificationTimeout."
                  )
                )

end Main
