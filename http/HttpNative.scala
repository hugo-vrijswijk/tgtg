//> using target.platform native
package tgtg.http

import cats.effect.kernel.Async
import cats.effect.{IO, Resource}
import sttp.client4.Backend
import sttp.client4.curl.AbstractCurlBackend
import sttp.client4.impl.cats.CatsMonadAsyncError
import sttp.client4.wrappers.FollowRedirectsBackend

def httpBackend: Resource[IO, Backend[IO]] = Resource.pure(CatsCurlBackend(true))

class CatsCurlBackend[F[_]: Async](verbose: Boolean)
    extends AbstractCurlBackend(new CatsMonadAsyncError[F], verbose)
    with Backend[F]

object CatsCurlBackend:
  def apply[F[_]: Async](verbose: Boolean = false): Backend[F] = FollowRedirectsBackend(new CatsCurlBackend[F](verbose))
