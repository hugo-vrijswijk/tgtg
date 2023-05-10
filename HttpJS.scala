import cats.effect.{Async, Resource, Sync}
import cats.syntax.apply.*
import sttp.client4.impl.cats.FetchCatsBackend

import scala.scalajs.js
import scala.scalajs.js.annotation.*
import sttp.client4.WebSocketBackend

def httpBackend[F[_]](using F: Async[F]): Resource[F, WebSocketBackend[F]] = Resource.pure(FetchCatsBackend[F]())

def loadConfig[F[_]](using F: Sync[F]): F[Unit] = F.delay {
  Dotenv.config()
}

@js.native
@JSImport("dotenv", JSImport.Namespace)
object Dotenv extends js.Object:
  def config(): Unit = js.native
end Dotenv
