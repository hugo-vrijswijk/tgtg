//> using target.platform js

package tgtg.http

import cats.effect.{IO, Resource}
import sttp.client4.WebSocketBackend
import sttp.client4.impl.cats.FetchCatsBackend

def httpBackend: Resource[IO, WebSocketBackend[IO]] = Resource.pure(FetchCatsBackend[IO]())
