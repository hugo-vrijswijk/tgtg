//> using target.platform native
package sttp.client4.curl.cats

import cats.effect.kernel.Async
import sttp.client4.Backend
import sttp.client4.curl.AbstractCurlBackend
import sttp.client4.impl.cats.CatsMonadAsyncError
import sttp.client4.wrappers.FollowRedirectsBackend
import sttp.client4.curl.internal.*
import sttp.client4.curl.*
import sttp.client4.curl.internal.CurlApi.*

class CatsCurlBackend[F[_]: Async](verbose: Boolean)
    extends AbstractCurlBackend(new CatsMonadAsyncError[F], verbose)
    with Backend[F]:
  override def performCurl(c: CurlHandle): F[CurlCode.CurlCode] = monad.blocking(c.perform)

object CatsCurlBackend:
  def apply[F[_]: Async](verbose: Boolean = false): Backend[F] = FollowRedirectsBackend(new CatsCurlBackend[F](verbose))
