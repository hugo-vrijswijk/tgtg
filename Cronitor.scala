import cats.FlatMap
import cats.effect.Resource
import cats.effect.Resource.ExitCase
import cats.syntax.functor.*
import cats.syntax.option.*
import sttp.client4.*

def cronitor[F[_]: FlatMap](http: Backend[F], cronitorToken: CronitorToken): Resource[F, Unit] =

  type State = "run" | "fail" | "complete"

  def reportState(state: State, message: Option[String] = none) =
    val cronitorUri = uri"https://cronitor.link/p/$cronitorToken?state=$state&message=$message"
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
