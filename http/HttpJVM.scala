//> using target.platform jvm
import cats.effect.Async
import sttp.client4.httpclient.cats.HttpClientCatsBackend

def httpBackend[F[_]: Async] = HttpClientCatsBackend.resource[F]()
