package tgtg.cache

import cats.Show
import cats.effect.IO
import cats.syntax.all.*
import io.circe.{Codec, Decoder, Encoder}
import org.legogroup.woof.{Logger, given}

import scala.concurrent.duration.FiniteDuration

trait CacheService(using log: Logger[IO]):

  def get[T: Decoder](key: String): IO[Option[T]]
  def set[T: Encoder](value: T, key: String, ttl: FiniteDuration): IO[Unit]

  def retrieveOrSet[T: Codec](action: IO[T], key: String, ttl: T => FiniteDuration): IO[T] =
    get(key).attempt.map(_.toOption.flatten).flatMap {
      case Some(value) =>
        // Cache hit
        log.debug(show"Cache hit for key '$key'").as(value)
      case None =>
        // Cache miss
        log.debug(show"Cache miss for key '$key'") *>
          action.flatTap(t => set(t, key, ttl(t)).attempt.void)
    }
end CacheService
