package tgtg

import cats.effect.Resource.ExitCase
import cats.effect.{IO, Resource}
import cats.syntax.all.*
import sttp.client4.*

def cronitor(http: Backend[IO], cronitorToken: ApiToken): Resource[IO, Unit] =

  type State = "run" | "fail" | "complete"

  def reportState(state: State, message: Option[String] = none) =
    val cronitorUri = uri"https://cronitor.link/p/$cronitorToken?state=$state&message=$message"
    basicRequest
      .post(cronitorUri)
      .response(asStringOrFail)
      .send(http)
      .void

  end reportState

  Resource.makeCase(reportState("run")):
    case (_, ExitCase.Succeeded)  => reportState("complete")
    case (_, ExitCase.Canceled)   => reportState("fail", "Canceled".some)
    case (_, ExitCase.Errored(e)) => reportState("fail", e.toString().some)

end cronitor
