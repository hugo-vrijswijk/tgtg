//> using target.platform native
package tgtg.http

import cats.effect.{IO, Resource}
import sttp.client4.curl.cats.CatsCurlBackend
import sttp.client4.Backend

def httpBackend: Resource[IO, Backend[IO]] = Resource.pure(CatsCurlBackend())
