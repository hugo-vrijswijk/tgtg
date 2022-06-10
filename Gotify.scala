import cats.effect.IO
import org.legogroup.woof.{Logger, given}
import sttp.client3.*
import sttp.client3.circe.*

class Gotify(using HttpBackend, Logger[IO]):
  private val baseUri    = uri"https://gotify.hugovr.dev"
  private val messageUri = uri"$baseUri/message"

  def sendNotification(msg: GotifyMessage) =
    basicRequest
      .post(messageUri)
      .header("X-Gotify-Key", secrets.gotifyKey)
      .responseGetRight
      .body(msg)
      .send(summon[HttpBackend])
      .logTimed("Gotify notification")
      .void
end Gotify
