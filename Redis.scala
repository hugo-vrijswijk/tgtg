import cats.data.Chain
import cats.effect.{Async, Sync}
import cats.syntax.applicativeError.*
import cats.syntax.apply.*
import cats.syntax.flatMap.*
import cats.syntax.functor.*
import cats.syntax.monadError.*
import cats.syntax.option.*
import cats.syntax.parallel.*
import cats.syntax.show.*
import cats.syntax.traverse.*
import cats.{Parallel, Show}
import io.chrisdavenport.rediculous.*
import org.legogroup.woof.{Logger, given}
import scodec.Codec
import scodec.bits.ByteVector
import scodec.codecs.utf8

import scala.concurrent.duration.FiniteDuration

import RedisCommands.SetOpts

class CacheService[F[_]: Async: Parallel](client: RedisConnection[F])(using Logger[F]):
  type RedisF[A] = Redis[F, A]
  given Show[Throwable] = Show.fromToString

  def get[T: Codec](key: String): F[Option[T]] =
    keyToBV(key)
      .map(RedisCommands.getBV[RedisF](_))
      .flatMap(_.run(client))
      .flatMap(_.traverse(decode)) // Raise an error if decoding fails
      .handleErrorWith(e => Logger[F].error(show"Failed to get key '$key': $e").as(none))
      .logTimed(show"getting '$key'")

  def set[T: Codec](value: T, key: String, ttl: FiniteDuration): F[Unit] =
    (keyToBV(key), encode(value)).parTupled
      .map((k, v) => RedisCommands.setBV[RedisF](k, v, SetOpts.default.copy(setSeconds = ttl.toSeconds.some)))
      .flatMap(_.run(client))
      .redeemWith(
        e => Logger[F].error(show"Failed to set key '$key': $e").void,
        _ => Sync[F].unit
      )
      .logTimed(show"setting '$key' with ttl of $ttl")
  end set

  private def keyToBV(key: String): F[ByteVector] =
    Sync[F].defer(Sync[F].fromTry(utf8.encode(key).toTry).map(_.toByteVector))
  private def encode[T: Codec](value: T) =
    Sync[F].defer(Sync[F].fromTry(summon[Codec[T]].encode(value).toTry).map(_.toByteVector))
  private def decode[T: Codec](bv: ByteVector): F[T] =
    Sync[F].defer(Sync[F].fromTry(summon[Codec[T]].decodeValue(bv.toBitVector).toTry))

  def retrieveOrSet[T: Codec](action: F[T], key: String, ttl: T => FiniteDuration): F[T] =
    get(key).flatMap {
      case Some(value) =>
        // Cache hit
        Logger[F].debug(show"Cache hit for key '$key'").as(value)
      case None =>
        // Cache miss
        Logger[F].debug(show"Cache miss for key '$key'") *>
          action.flatTap(t => set(t, key, ttl(t)))
    }
end CacheService
