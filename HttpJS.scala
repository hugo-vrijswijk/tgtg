import cats.effect.{Async, Resource, Sync}
import sttp.client4.impl.cats.FetchCatsBackend

import scala.scalajs.js
import scala.scalajs.js.annotation.*

def httpBackend[F[_]: Async] = Resource
  .pure(FetchCatsBackend[F]())
  .evalTap(_ =>
    Sync[F].delay {
      val g = js.Dynamic.global.globalThis

      if js.isUndefined(g.fetch) then
        g.fetch = NodeFetch.default
        g.Headers = NodeFetch.Headers
        g.Request = NodeFetch.Request
        g.Response = NodeFetch.Response
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
