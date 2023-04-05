import cats.FlatMap
import cats.effect.Resource
import cats.effect.Resource.ExitCase
import cats.effect.std.Env
import cats.syntax.flatMap.*
import cats.syntax.functor.*
import cats.syntax.option.*
import sttp.client4.*
import sttp.model.*

def cronitor[F[_]: FlatMap: Env](http: Backend[F]): Resource[F, Unit] =
  def baseUri = secrets.cronitor.map(u => uri"https://cronitor.link/p/$u")

  type State = "run" | "fail" | "complete"

  def reportState(state: State, message: Option[String] = none) =
    baseUri.map(base => uri"$base?state=$state&message=$message").flatMap { cronitorUri =>
      basicRequest
        .post(cronitorUri)
        .responseGetRight
        .send(http)
        .void
    }
  end reportState

  Resource.makeCase(reportState("run")) {
    case (_, ExitCase.Succeeded)  => reportState("complete")
    case (_, ExitCase.Canceled)   => reportState("fail", "Canceled".some)
    case (_, ExitCase.Errored(e)) => reportState("fail", e.toString().some)
  }
end cronitor
