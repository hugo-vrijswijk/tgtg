import cats.effect.syntax.all.*
import cats.effect.{IO, Resource}
import org.legogroup.woof.Logger
import sttp.client3.impl.cats.FetchCatsBackend

import scala.scalajs.js
import scala.scalajs.js.annotation.*

def httpBackend(using Logger[IO]) = Resource
  .pure(FetchCatsBackend[IO]())
  .evalTap(_ =>
    IO {
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
