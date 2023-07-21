import cats.FlatMap
import cats.effect.Clock
import cats.syntax.functor.*
import org.legogroup.woof.{Logger, given}
import sttp.client4.*
import sttp.client4.circe.*

class Gotify[F[_]: FlatMap: Clock: Logger](http: Backend[F], config: GotifyConfig):

  def sendNotification(msg: GotifyMessage) =
    basicRequest
      .post(uri"${config.url}/message")
      .header("X-Gotify-Key", config.token)
      .responseGetRight
      .body(msg)
      .send(http)
      .logTimed("Gotify notification")
      .void

end Gotify
