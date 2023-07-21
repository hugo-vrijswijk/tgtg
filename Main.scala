import cats.Parallel
import cats.effect.syntax.all.*
import cats.effect.{Async, ExitCode, IO, Resource}
import cats.syntax.all.*
import com.comcast.ip4s.*
import com.monovore.decline.Opts
import com.monovore.decline.effect.CommandIOApp
import io.chrisdavenport.rediculous.RedisConnection
import org.legogroup.woof.{Logger, given}
import sttp.client4.logging.{LogConfig, LoggingBackend}
import sttp.model.HeaderNames

import scala.concurrent.duration.*

object Main extends CommandIOApp("tgtg", "TooGoodToGo notifier"):

  override def main: Opts[IO[ExitCode]] = Config.opts.map(config =>
    makeLogger
      .flatMap { implicit logger =>
        Main.runF[IO](config)
      }
      .as(ExitCode.Success)
  )

  def runF[F[_]: Async: Logger: Parallel](config: Config) =
    (
      wrappedBackend.flatTap(b => config.cronitor.fold(Logger[F].info("Cronitor disabled").toResource)(cronitor(b, _))),
      redisConnection
    ).parTupled.use { (http, redisConnection) =>

      val redis  = CacheService(redisConnection)
      val tgtg   = TooGoodToGo(redis, http, config.tgtg)
      val gotify = Gotify(http, config.gotify)

      tgtg.getItems
        .map(_.filter(_.items_available > 0))
        .flatMap { items =>
          if items.isEmpty then Logger[F].info("No boxes to notify for.")
          else
            items.parTraverse_ { item =>
              val key = show"gotify-${item.store.store_name}"

              val notificationTimeout = 60.minutes

              redis
                .get[Boolean](key)
                .attempt
                .map(_.toOption.flatten)
                .map(!_.contains(true))
                .ifM(
                  redis
                    .set(true, key, notificationTimeout)
                    .guarantee(
                      gotify.sendNotification(GotifyMessage(title = item.display_name, message = item.show))
                    ),
                  Logger[F]
                    .info(
                      show"Found boxes, but notification for '$key' was already sent in the last $notificationTimeout."
                    )
                )
            }
        }
    }

  def redisConnection[F[_]: Async] = RedisConnection.queued[F].withHost(host"192.168.87.29").build

  def wrappedBackend[F[_]: Async: Logger] = httpBackend[F].map(
    LoggingBackend(
      _,
      new LogWrapper,
      LogConfig(sensitiveHeaders =
        HeaderNames.SensitiveHeaders ++ Set("X-Gotify-Key", "x-amz-cf-id", "x-correlation-id")
      )
    )
  )

end Main
