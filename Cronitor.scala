import cats.effect.kernel.Resource.ExitCase
import cats.effect.{IO, Resource}
import sttp.client3.*
import sttp.client3.circe.*
import sttp.model.*

def cronitor(http: HttpBackend): Resource[IO, Unit] =
  val baseUri = uri"https://cronitor.link/p/${secrets.cronitor}"

  type State = "run" | "fail" | "complete"

  def reportState(s: State, message: Option[String] = None) =
    def state = uri"$baseUri?state=$s&message=$message"

    basicRequest
      .post(state)
      .responseGetRight
      .send(http)
      .void
  end reportState

  Resource.makeCase(reportState("run")) {
    case (_, ExitCase.Succeeded)  => reportState("complete")
    case (_, ExitCase.Canceled)   => reportState("fail")
    case (_, ExitCase.Errored(e)) => reportState("fail", Some(e.getMessage()))
  }
end cronitor
