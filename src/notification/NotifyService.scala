package tgtg.notification

import cats.effect.IO
import cats.syntax.show.*
import io.circe.Encoder
import io.github.iltotore.iron.*
import io.github.iltotore.iron.circe.given
import io.github.iltotore.iron.constraint.numeric.Positive0
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
  def simple[T: Encoder](name: String :| NotEmpty, uri: Uri, http: Backend[IO], headers: Header*)(
      messageFn: (Title, Message) => T
  )(using Logger[IO]): NotifyService = (title: Title, message: Message) =>
    basicRequest
      .post(uri)
      .headers(headers*)
      .response(asStringOrFail)
      .body(asJson(messageFn(title, message)))
      .send(http)
      .logTimed(show"$name notification".assume[NotEmpty])
      .void
end NotifyService

object Title extends RefinedType[String, NotEmpty]
type Title = Title.T

object Message extends RefinedType[String, NotEmpty]
type Message = Message.T

enum NotifyConfig:
  case Gotify(token: ApiToken, url: Uri)
  case Pushbullet(token: ApiToken)
  case Pushover(token: ApiToken, user: UserId)
  case Webhook(url: Uri)
end NotifyConfig

case class GotifyMessage(title: Title, message: Message, priority: Int :| Positive0 = 8) derives Encoder.AsObject
case class PushbulletMessage(title: Title, body: Message, `type`: String :| NotEmpty = "note") derives Encoder.AsObject
case class PushoverMessage(title: Title, message: Message, token: ApiToken, user: UserId) derives Encoder.AsObject
case class WebhookMessage(title: Title, message: Message) derives Encoder.AsObject
