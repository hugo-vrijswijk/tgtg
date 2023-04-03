import cats.effect.kernel.Resource.ExitCase
import cats.effect.{IO, Resource}
import cats.syntax.option.*
import sttp.client4.*
import sttp.client4.circe.*
import sttp.model.*

def cronitor(http: Backend[IO]): Resource[IO, Unit] =
  val baseUri = uri"https://cronitor.link/p/${secrets.cronitor}"

  type State = "run" | "fail" | "complete"

  def reportState(state: State, message: Option[String] = none) =
    val cronitorUri = uri"$baseUri?state=$state&message=$message"

    basicRequest
      .post(cronitorUri)
      .responseGetRight
      .send(http)
      .void
  end reportState

  Resource.makeCase(reportState("run")) {
    case (_, ExitCase.Succeeded)  => reportState("complete")
    case (_, ExitCase.Canceled)   => reportState("fail", "Canceled".some)
    case (_, ExitCase.Errored(e)) => reportState("fail", e.toString().some)
  }
end cronitor
