package tgtg

import cats.data.{Ior, NonEmptyList}
import cats.effect.{ExitCode, IO}
import cats.syntax.all.*
import com.monovore.decline.Opts
import com.monovore.decline.effect.CommandIOApp
import cron4s.CronExpr
import eu.timepit.fs2cron.cron4s.Cron4sScheduler
import fs2.Stream
import io.github.iltotore.iron.*
import io.github.iltotore.iron.cats.given
import org.legogroup.woof.{Logger, given}
import tgtg.cache.CacheKey
import tgtg.notification.Title

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
            main.logDeprecations
              *> main.runOrServer
                .as(ExitCode.Success)
                .handleErrorWithLog
        end match
  }
end Main

final class Main(config: Config)(using log: Logger[IO]):

  def runOrServer =
    config.server match
      // isServer is deprecated, but used: run with default interval
      case ServerConfig(None, None, true) =>
        loop(NonEmptyList.of(5.minutes.asLeft))
      case ServerConfig(intervals, crons, _) =>
        // If intervals or crons is defined, run with them. Otherwise, run once
        val maybeLoop = Ior
          .fromOptions(intervals, crons)
          .map: ior =>
            val nel = ior.bimap(_.map(_.asLeft), _.map(_.asRight)).merge
            loop(nel)

        maybeLoop.getOrElse(run)

  /** Run the main loop (log errors, never exit)
    */
  def loop(intervals: NonEmptyList[Either[FiniteDuration, CronExpr]]): IO[Unit] =
    val scheduler = Cron4sScheduler.systemDefault[IO]

    // Run, and find the minimum next interval and log it
    val runAndLog: IO[Unit] = run.handleErrorWithLog.void >> (
      intervals
        .parTraverse(_.fold(_.pure[IO], scheduler.fromNowUntilNext))
        .map(_.minimum)
        .flatMap(nextInterval => log.info(show"Sleeping for $nextInterval"))
    )

    val streams: List[Stream[IO, Unit]] = intervals
      .map(
        _.fold(
          Stream.awakeEvery[IO](_),
          scheduler.awakeEvery(_)
        ) >> Stream.eval(runAndLog)
      )
      .toList

    // Run for the first time, then run every interval
    (log.info("Starting tgtg notifier") *>
      log.info(show"Intervals: ${intervals.map(_.fold(_.show, _.show)).mkString_(", ")}") *>
      runAndLog *> Stream.emits(streams).parJoinUnbounded.compile.drain)
      .guarantee(log.info("Shutting down") *> log.info("Bye!"))
  end loop

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
              val key = CacheKey.assume(s"gotify-${item.store.store_name}")

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
                      notify.sendNotification(Title(item.display_name), item.message)
                    ),
                  log.info(
                    show"Found boxes, but notification for '$key' was already sent in the last $notificationTimeout."
                  )
                )
          end if

  def logDeprecations = IO.whenA(config.server.isServer)(
    log.warn("The --server flag is deprecated. Use --interval or --cron options instead.")
  )
end Main
