//> using target.platform jvm
import cats.effect.Async
import org.legogroup.woof.Logger
import sttp.client4.httpclient.cats.HttpClientCatsBackend

def httpBackend[F[_]: Async](using Logger[F]) = HttpClientCatsBackend.resource[F]()
