import cats.syntax.applicativeError.*
import cats.syntax.apply.*
import cats.syntax.flatMap.*
import cats.syntax.functor.*
import cats.syntax.show.*
import cats.{MonadThrow, Show}
import io.circe.{Codec, Decoder, Encoder}
import org.legogroup.woof.{Logger, given}

import scala.concurrent.duration.FiniteDuration

trait CacheService[F[_]: MonadThrow: Logger]:
  def get[T: Decoder](key: String): F[Option[T]]
  def set[T: Encoder](value: T, key: String, ttl: FiniteDuration): F[Unit]
  def retrieveOrSet[T: Codec](action: F[T], key: String, ttl: T => FiniteDuration): F[T] =
    get(key).attempt.map(_.toOption.flatten).flatMap {
      case Some(value) =>
        // Cache hit
        Logger[F].debug(show"Cache hit for key '$key'").as(value)
      case None =>
        // Cache miss
        Logger[F].debug(show"Cache miss for key '$key'") *>
          action.flatTap(t => set(t, key, ttl(t)).attempt.void)
    }
end CacheService
