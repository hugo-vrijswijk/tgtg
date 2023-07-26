package tgtg.cache

import cats.Show
import cats.effect.IO
import cats.syntax.all.*
import io.chrisdavenport.rediculous.*
import io.circe.parser.*
import io.circe.syntax.*
import io.circe.{Decoder, Encoder}
import org.legogroup.woof.{Logger, given}
import tgtg.{*, given}

import scala.concurrent.duration.FiniteDuration

import RedisCommands.SetOpts

class RedisCacheService(client: RedisConnection[IO])(using log: Logger[IO]) extends CacheService:

  private type RedisF[A] = Redis[IO, A]

  def get[T: Decoder](key: CacheKey): IO[Option[T]] =
    RedisCommands
      .get[RedisF](key)
      .run(client)
      .flatMap(_.traverse(t => IO.fromEither(decode[T](t))))
      .handleErrorWith(e => log.error(show"Failed to get key '$key': $e").as(none))
      .logTimed(show"getting '$key'")

  def set[T: Encoder](value: T, key: CacheKey, ttl: FiniteDuration): IO[Unit] =
    RedisCommands
      .set[RedisF](key, value.asJson.noSpaces, SetOpts.default.copy(setSeconds = ttl.toSeconds.some))
      .run(client)
      .redeemWith(
        e => log.error(show"Failed to set key '$key': $e").void,
        _ => IO.unit
      )
      .logTimed(show"setting '$key' with ttl of $ttl")
  end set

end RedisCacheService
