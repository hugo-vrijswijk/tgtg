import cats.Show
import cats.effect.Async
import cats.syntax.applicativeError.*
import cats.syntax.flatMap.*
import cats.syntax.functor.*
import cats.syntax.monadError.*
import cats.syntax.option.*
import cats.syntax.show.*
import cats.syntax.traverse.*
import io.chrisdavenport.rediculous.*
import io.circe.parser.*
import io.circe.syntax.*
import io.circe.{Decoder, Encoder}
import org.legogroup.woof.{Logger, given}

import scala.concurrent.duration.FiniteDuration

import RedisCommands.SetOpts

class RedisCacheService[F[_]](client: RedisConnection[F])(using F: Async[F], log: Logger[F]) extends CacheService[F]:

  type RedisF[A] = Redis[F, A]
  given Show[Throwable] = Show.fromToString

  def get[T: Decoder](key: String): F[Option[T]] =
    RedisCommands
      .get[RedisF](key)
      .run(client)
      .flatMap(_.traverse(t => F.fromEither(decode[T](t))))
      .handleErrorWith(e => log.error(show"Failed to get key '$key': $e").as(none))
      .logTimed(show"getting '$key'")

  def set[T: Encoder](value: T, key: String, ttl: FiniteDuration): F[Unit] =
    RedisCommands
      .set[RedisF](key, value.asJson.noSpaces, SetOpts.default.copy(setSeconds = ttl.toSeconds.some))
      .run(client)
      .redeemWith(
        e => log.error(show"Failed to set key '$key': $e").void,
        _ => F.unit
      )
      .logTimed(show"setting '$key' with ttl of $ttl")
  end set

end RedisCacheService
