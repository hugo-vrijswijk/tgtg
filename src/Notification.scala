package tgtg

import cats.data.NonEmptyList
import cats.effect.IO
import cats.syntax.all.*
import io.circe.*
import io.github.iltotore.iron.*
import io.github.iltotore.iron.cats.given
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

  def telegram(token: ApiToken): Notification = TelegramNotification(token)

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

// https://core.telegram.org/bots/api
private class TelegramNotification(token: ApiToken) extends Notification:

  override def sendNotification(http: Backend[IO], title: Title, message: Message)(using Logger[IO]): IO[Unit] =
    basicRequest
      .post(uri("getUpdates"))
      .response(asJsonOrFail[GetUpdatesResponse])
      .body(asJson(GetUpdatesMesage(NonEmptyList.one("message"))))
      .send(http)
      .map(_.body.result.map(_.message.chat.id))
      .flatMap:
        case Nil => IO.raiseError(new Exception("Couldn't find chat, please start a conversation with the bot first"))
        case userIds =>
          userIds.parTraverseVoid: userId =>
            Logger[IO].debug(show"Found Telegram chat id: $userId".assume[NotEmpty]) *>
              basicRequest
                .post(uri("sendMessage"))
                .response(asStringAlways)
                .body(
                  asJson(
                    TelegramSendMessage(
                      chat_id = userId,
                      text = show"$title\n\n$message".assume[NotEmpty]
                    )
                  )
                )
                .send(http)
      .logTimed("Telegram notification")

  override def name = "Telegram"

  private def uri(method: String): Uri = uri"https://api.telegram.org/bot$token/$method"

  private case class GetUpdatesMesage(allowed_updates: NonEmptyList[String :| NotEmpty]) derives Encoder.AsObject
  private case class TelegramSendMessage(chat_id: Long, text: String :| NotEmpty) derives Encoder.AsObject

  private case class GetUpdatesResponse(ok: Boolean, result: List[UpdateResponse]) derives Decoder
  private case class UpdateResponse(message: TelegramMessageResponse) derives Decoder
  private case class TelegramMessageResponse(chat: ChatResponse) derives Decoder
  private case class ChatResponse(id: Long) derives Decoder

end TelegramNotification
