//> using target.platform jvm

package tgtg.http

import cats.effect.{IO, Resource}
import sttp.client4.WebSocketBackend
import sttp.client4.httpclient.cats.HttpClientCatsBackend

def httpBackend: Resource[IO, WebSocketBackend[IO]] = HttpClientCatsBackend.resource[IO]()
