import cats.Parallel
import cats.effect.syntax.all.*
import cats.effect.{Async, ExitCode, IO, Resource}
import cats.syntax.all.*
import com.monovore.decline.Opts
import com.monovore.decline.effect.CommandIOApp
import io.chrisdavenport.rediculous.RedisConnection
import org.legogroup.woof.{Logger, given}
import sttp.client4.logging.{LogConfig, LoggingBackend}
import sttp.model.HeaderNames

import scala.concurrent.duration.*

object Main extends CommandIOApp("tgtg", "TooGoodToGo notifier"):

  override def main: Opts[IO[ExitCode]] = Config.opts.map(config =>
    makeLogger(config.log)
      .flatMap { implicit logger =>
        Main.runF[IO](config)
      }
      .as(ExitCode.Success)
  )

  def runF[F[_]: Async: Logger: Parallel](config: Config) =
    (mkHttpBackend(config.cronitor), mkCache(config.redis)).parTupled.use { (http, cache) =>

      val tgtg   = TooGoodToGo(cache, http, config.tgtg)
      val gotify = Gotify(http, config.gotify)

      tgtg.getItems
        .map(_.filter(_.items_available > 0))
        .flatMap { items =>
          if items.isEmpty then Logger[F].info("No boxes to notify for.")
          else
            items.parTraverse_ { item =>
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

  def mkHttpBackend[F[_]: Async: Logger](config: Option[CronitorToken]) =
    wrappedBackend.flatTap(b => config.fold(Logger[F].info("Cronitor disabled").toResource)(cronitor(b, _)))

  def mkCache[F[_]: Async: Logger](config: Option[RedisConfig]): Resource[F, CacheService[F]] = config match
    case None => Resource.eval(Logger[F].info("Using cache.json")) *> FileCacheService.create
    case Some(config) =>
      Resource.eval(Logger[F].info("Using Redis cache")) *> RedisConnection
        .queued[F]
        .withHost(config.host)
        .build
        .map(new RedisCacheService(_))

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
