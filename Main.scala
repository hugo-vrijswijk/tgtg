import cats.Parallel
import cats.effect.std.Random
import cats.effect.{Async, IOApp}
import cats.syntax.apply.*
import cats.syntax.flatMap.*
import cats.syntax.functor.*
import cats.syntax.parallel.*
import cats.syntax.show.*
import com.comcast.ip4s.*
import io.chrisdavenport.rediculous.RedisConnection
import org.legogroup.woof.{Logger, given}
import sttp.client4.Backend
import sttp.client4.logging.{LogConfig, LoggingBackend}
import sttp.model.HeaderNames

import scala.concurrent.duration.*

object Main extends IOApp.Simple:
  val run = makeLogger.flatMap(implicit logger => runF)

  def runF[F[_]: Async: Logger: Parallel] = (wrappedBackend.flatTap(cronitor), redisConnection).parTupled.use {
    (http, redisConnection) =>

      val redis  = CacheService(redisConnection)
      val tgtg   = TooGoodToGo(redis, http)
      val gotify = Gotify(http)

      tgtg.getItems
        .map(_.filter(_.items_available > 0))
        .flatMap { items =>
          if items.isEmpty then Logger[F].info("No boxes to notify for.")
          else
            items.parTraverse_ { item =>
              val key = show"gotify-${item.store.store_name}"

              val notificationTimeout = 40.minutes

              redis
                .get[Boolean](key)
                .map(!_.isDefined)
                .ifM(
                  redis.set(true, key, notificationTimeout) *>
                    gotify.sendNotification(GotifyMessage(title = item.display_name, message = item.show)),
                  Logger[F]
                    .info(
                      show"Found boxes, but notification for '$key' was already sent in the last $notificationTimeout."
                    )
                )
            }
        }
  }

  def redisConnection[F[_]: Async] = RedisConnection.queued[F].withHost(host"192.168.87.29").build

  def wrappedBackend[F[_]: Async](using Logger[F]) = httpBackend[F].map(
    LoggingBackend(
      _,
      new LogWrapper,
      LogConfig(sensitiveHeaders =
        HeaderNames.SensitiveHeaders ++ Set("X-Gotify-Key", "x-amz-cf-id", "x-correlation-id")
      )
    )
  )

end Main
