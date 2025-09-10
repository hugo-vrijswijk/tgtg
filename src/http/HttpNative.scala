//> using target.platform native

package tgtg.http

import cats.effect.{IO, Resource}
import sttp.client4.Backend
import sttp.client4.curl.cats.CurlCatsBackend

def httpBackend: Resource[IO, Backend[IO]] = Resource.pure(CurlCatsBackend[IO]())
