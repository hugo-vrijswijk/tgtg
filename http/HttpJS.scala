//> using target.platform js
import cats.effect.{Async, Resource}
import sttp.client4.impl.cats.FetchCatsBackend

import sttp.client4.WebSocketBackend

def httpBackend[F[_]: Async]: Resource[F, WebSocketBackend[F]] = Resource.pure(FetchCatsBackend[F]())
