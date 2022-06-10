//> using scala "3"
//> using lib "co.fs2::fs2-core::3.2.8"
//> using lib "org.typelevel::cats-effect::3.3.12"
//> using lib "com.softwaremill.sttp.client3::circe::3.6.2"
//> using lib "io.chrisdavenport::rediculous::0.3.2"
//> using lib "io.circe::circe-core::0.14.2"
//> using lib "io.circe::circe-generic::0.14.2"
//> using lib "io.circe::circe-parser::0.14.2"
//> using lib "org.scodec::scodec-core::2.1.0"
//> using lib "org.legogroup::woof-core::0.4.4"

import cats.effect.*
import cats.syntax.all.*
import com.comcast.ip4s.*
import io.chrisdavenport.rediculous.RedisConnection
import org.legogroup.woof.{Logger, given}
import sttp.capabilities.WebSockets
import sttp.client3.SttpBackend
import sttp.client3.logging.LoggingBackend
import sttp.model.HeaderNames

import java.time.LocalDate
import scala.concurrent.duration.*

object Main extends IOApp.Simple:
  val run =
    makeLogger.flatMap { implicit logger =>
      val resources = (wrappedBackend, redisConnection).parTupled.flatMap { (http, redis) =>
        cronitor(http).as((http, redis))
      }

      resources.use { (http, redisConnection) =>
        given HttpBackend = http
        val redis         = CacheService(redisConnection)
        val tgtg          = TooGoodToGo(redis)
        val gotify        = Gotify()

        tgtg.getItems
          .map(_.filter(_.items_available > 0))
          .flatMap { items =>
            if items.isEmpty then Logger[IO].info("No boxes to notify for.")
            else
              items.parTraverse_ { item =>
                val key = show"gotify-${item.store.store_name}"

                redis
                  .get[Boolean](key)
                  .map(!_.isDefined)
                  .ifM(
                    redis.set(true, key, 30.minutes) *>
                      gotify.sendNotification(GotifyMessage(title = item.display_name, message = item.show)),
                    Logger[IO]
                      .info(show"Found boxes, but notification for '$key' was already sent in the last 30 minutes")
                  )
              }
          }
      }
    }

  end run

  def redisConnection = RedisConnection.queued[IO].withHost(host"192.168.87.29").build

  def wrappedBackend(using Logger[IO]) = httpBackend.map(
    LoggingBackend(
      _,
      new LogWrapper,
      sensitiveHeaders = HeaderNames.SensitiveHeaders + "X-Gotify-Key"
    )
  )

end Main

type HttpBackend = SttpBackend[cats.effect.IO, WebSockets]
