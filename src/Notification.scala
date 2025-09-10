package tgtg

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

trait Notification:
  def sendNotification(http: Backend[IO], title: Title, message: Message)(using Logger[IO]): IO[Unit]
  def name: String :| NotEmpty

object Notification:

  // https://gotify.net/api-docs
  def gotify(token: ApiToken, url: Uri): Notification =
    SimpleNotification(
      name = "Gotify",
      uri = uri"$url/message",
      headers = Header("X-Gotify-Key", token.value)
    )(GotifyMessage(_, _))

  // https://docs.pushbullet.com/#create-push
  def pushbullet(token: ApiToken): Notification =
    SimpleNotification(
      name = "Pushbullet",
      uri = uri"https://api.pushbullet.com/v2/pushes",
      headers = Header("Access-Token", token.value)
    )(PushbulletMessage(_, _))

  // https://pushover.net/api#messages
  def pushover(token: ApiToken, user: UserId): Notification =
    SimpleNotification(
      name = "Pushover",
      uri = uri"https://api.pushover.net/1/messages.json"
    )((title, message) => PushoverMessage(title, message, token, user))

  def webhook(url: Uri): Notification =
    SimpleNotification(
      name = "Webhook",
      uri = url
    )(WebhookMessage(_, _))

  private case class GotifyMessage(title: Title, message: Message, priority: Int :| Positive0 = 8)
      derives Encoder.AsObject
  private case class PushbulletMessage(title: Title, body: Message, `type`: String :| NotEmpty = "note")
      derives Encoder.AsObject
  private case class PushoverMessage(title: Title, message: Message, token: ApiToken, user: UserId)
      derives Encoder.AsObject
  private case class WebhookMessage(title: Title, message: Message) derives Encoder.AsObject

  /** Create a simple [[Notification]] service that sends a HTTP request with a JSON body
    */
  private class SimpleNotification[T: Encoder](val name: String :| NotEmpty, uri: Uri, headers: Header*)(
      createBody: (Title, Message) => T
  ) extends Notification:
    def sendNotification(http: Backend[IO], title: Title, message: Message)(using Logger[IO]): IO[Unit] =
      basicRequest
        .post(uri)
        .headers(headers*)
        .response(asStringOrFail)
        .body(asJson(createBody(title, message)))
        .send(http)
        .logTimed(show"$name notification".assume[NotEmpty])
        .void
  end SimpleNotification
end Notification

object Title extends RefinedType[String, NotEmpty]
type Title = Title.T

object Message extends RefinedType[String, NotEmpty]
type Message = Message.T
