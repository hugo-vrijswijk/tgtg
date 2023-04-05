import cats.FlatMap
import cats.effect.Clock
import cats.syntax.functor.*
import org.legogroup.woof.{Logger, given}
import sttp.client4.*
import sttp.client4.circe.*

class Gotify[F[_]: FlatMap: Clock](http: Backend[F])(using Logger[F]):
  private val baseUri    = uri"https://gotify.hugovr.dev"
  private val messageUri = uri"$baseUri/message"

  def sendNotification(msg: GotifyMessage) =
    basicRequest
      .post(messageUri)
      .header("X-Gotify-Key", secrets.gotifyKey)
      .responseGetRight
      .body(msg)
      .send(http)
      .logTimed("Gotify notification")
      .void
end Gotify
