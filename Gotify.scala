import cats.FlatMap
import cats.effect.Clock
import cats.effect.std.Env
import cats.syntax.flatMap.*
import cats.syntax.functor.*
import org.legogroup.woof.{Logger, given}
import sttp.client4.*
import sttp.client4.circe.*

class Gotify[F[_]: FlatMap: Clock: Env: Logger](http: Backend[F]):
  private val baseUri    = uri"https://gotify.hugovr.dev"
  private val messageUri = uri"$baseUri/message"

  def sendNotification(msg: GotifyMessage) =
    secrets.gotifyKey.flatMap { key =>
      basicRequest
        .post(messageUri)
        .header("X-Gotify-Key", key)
        .responseGetRight
        .body(msg)
        .send(http)
        .logTimed("Gotify notification")
        .void
    }
end Gotify
