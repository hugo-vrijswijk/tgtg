package tgtg.notification

import cats.effect.IO
import io.circe.Encoder
import org.legogroup.woof.{Logger, given}
import sttp.client4.*
import sttp.client4.circe.*
import tgtg.*

class Gotify(http: Backend[IO], config: NotifyConfig.Gotify)(using Logger[IO]) extends NotifyService:

  override def sendNotification(notification: Notification): IO[Unit] =
    basicRequest
      .post(uri"${config.url}/message")
      .header("X-Gotify-Key", config.token)
      .responseGetRight
      .body(GotifyMessage(notification.message, notification.title))
      .send(http)
      .logTimed("Gotify notification")
      .void

  case class GotifyMessage(message: String, title: String, priority: Int = 8) derives Encoder.AsObject
end Gotify
