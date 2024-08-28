package tgtg

import cats.effect.{ExitCode, IO}
import cats.syntax.all.*
import com.monovore.decline.Opts
import com.monovore.decline.effect.CommandIOApp
import org.legogroup.woof.{Logger, given}
import tgtg.cache.CacheKey
import tgtg.notification.{Message, Title}

import scala.concurrent.duration.*

val version = new String(
  classOf[Main]
    .getClassLoader()
    .getResourceAsStream("version.txt")
    .readAllBytes()
).trim()

object Main extends CommandIOApp("tgtg", "TooGoodToGo notifier for your favourite stores", version = version):

  override def main: Opts[IO[ExitCode]] = (Config.opts orElse Config.auth).map { config =>
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
              .as(ExitCode.Success)
              .handleErrorWithLog
        end match
  }
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
        .flatMap: stores =>
          val items = stores.filter(_.items_available > 0)
          if items.isEmpty then log.info(show"No boxes to notify for (from ${stores.length} stores).")
          else
            items.parTraverse_ : item =>
              val key = CacheKey(show"gotify-${item.store.store_name}")

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
                      notify.sendNotification(Title(item.display_name), Message(item.show))
                    ),
                  log.info(
                    show"Found boxes, but notification for '$key' was already sent in the last $notificationTimeout."
                  )
                )
          end if

end Main
