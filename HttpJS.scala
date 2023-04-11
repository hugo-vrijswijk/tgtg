import cats.effect.{Async, Resource, Sync}
import cats.syntax.apply.*
import org.legogroup.woof.{Logger, given}
import sttp.client4.impl.cats.FetchCatsBackend

import scala.scalajs.js
import scala.scalajs.js.annotation.*

def httpBackend[F[_]](using F: Async[F], log: Logger[F]) = Resource
  .pure(FetchCatsBackend[F]())
  .evalTap(_ =>
    F.defer {
      val g = js.Dynamic.global.globalThis

      if js.isUndefined(g.fetch) then
        F.fromPromise(F.delay(js.dynamicImport {
          g.fetch = NodeFetch.default
          g.Headers = NodeFetch.Headers
          g.Request = NodeFetch.Request
          g.Response = NodeFetch.Response
        })) *> log.debug("polyfilled fetch with node-fetch")
      else log.debug("fetch already defined, not overwriting with node-fetch")
      end if
    }
  )

@js.native
trait NodeFetch extends js.Object:
  val default: js.Any  = js.native
  val Headers: js.Any  = js.native
  val Request: js.Any  = js.native
  val Response: js.Any = js.native
end NodeFetch

@js.native
@JSImport("node-fetch", JSImport.Namespace)
object NodeFetch extends NodeFetch

def loadConfig[F[_]](using F: Sync[F]): F[Unit] = F.delay {
  Dotenv.config()
}

@js.native
@JSImport("dotenv", JSImport.Namespace)
object Dotenv extends js.Object:
  def config(): Unit = js.native
end Dotenv
