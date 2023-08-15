package tgtg.notification

import cats.effect.IO
import io.circe.Encoder
import org.legogroup.woof.{Logger, given}
import sttp.client4.*
import sttp.client4.circe.*
import sttp.model.{Header, Uri}
import tgtg.*

trait NotifyService:
  def sendNotification(title: Title, message: Message): IO[Unit]

object NotifyService:

  /** Create a simple [[NotifyService]] that sends a HTTP request with a JSON body
    */
  def simple[T: Encoder](name: String, uri: Uri, http: Backend[IO], headers: Header*)(
      messageFn: (Title, Message) => T
  )(using Logger[IO]): NotifyService = (title: Title, message: Message) =>
    basicRequest
      .post(uri)
      .headers(headers*)
      .responseGetRight
      .body(messageFn(title, message))
      .send(http)
      .logTimed(s"$name notification")
      .void
end NotifyService

object Title extends NewType[String]
type Title = Title.Type

object Message extends NewType[String]
type Message = Message.Type

enum NotifyConfig:
  case Gotify(token: ApiToken, url: Uri)
  case Pushbullet(token: ApiToken)
  case Pushover(token: ApiToken, user: UserId)
  case Webhook(url: Uri)
end NotifyConfig

case class GotifyMessage(title: Title, message: Message, priority: Int = 8) derives Encoder.AsObject
case class PushbulletMessage(title: Title, body: Message, `type`: String = "note") derives Encoder.AsObject
case class PushoverMessage(title: Title, message: Message, token: ApiToken, user: UserId) derives Encoder.AsObject
case class WebhookMessage(title: Title, message: Message) derives Encoder.AsObject
