import cats.Show
import cats.data.Chain
import cats.effect.IO
import cats.syntax.option.*
import cats.syntax.parallel.*
import cats.syntax.show.*
import cats.syntax.traverse.*
import io.chrisdavenport.rediculous.RedisCommands.SetOpts
import io.chrisdavenport.rediculous.*
import org.legogroup.woof.{Logger, given}
import scodec.Codec
import scodec.bits.ByteVector
import scodec.codecs.utf8

import scala.concurrent.duration.FiniteDuration

class CacheService(client: RedisConnection[IO])(using Logger[IO]):

  given Show[Throwable] = Show.fromToString

  def get[T: Codec](key: String): IO[Option[T]] =
    keyToBV(key)
      .map(RedisCommands.getBV[RedisIO](_))
      .flatMap(_.run(client))
      .flatMap(_.traverse(decode)) // Raise an error if decoding fails
      .handleErrorWith(e => Logger[IO].error(show"Failed to get key '$key': $e").as(none))
      .logTimed(show"getting '$key'")
  
  def set[T: Codec](value: T, key: String, ttl: FiniteDuration): IO[Unit] =
    (keyToBV(key), encode(value)).parTupled
      .map((k, v) => RedisCommands.setBV[RedisIO](k, v, SetOpts.default.copy(setSeconds = ttl.toSeconds.some)))
      .flatMap(_.run(client))
      .redeemWith(
        e => Logger[IO].error(show"Failed to set key '$key': $e").void,
        _ => IO.unit
      )
      .logTimed(show"setting '$key' with ttl of $ttl")
  end set

  private def keyToBV(key: String): IO[ByteVector] = IO.defer(IO.fromTry(utf8.encode(key).toTry).map(_.toByteVector))
  private def encode[T: Codec](value: T) =
    IO.defer(IO.fromTry(summon[Codec[T]].encode(value).toTry).map(_.toByteVector))
  private def decode[T: Codec](bv: ByteVector): IO[T] =
    IO.defer(IO.fromTry(summon[Codec[T]].decodeValue(bv.toBitVector).toTry))

  def retrieveOrSet[T: Codec](action: IO[T], key: String, ttl: T => FiniteDuration): IO[T] =
    get(key).flatMap {
      case Some(value) =>
        // Cache hit
        Logger[IO].debug(show"Cache hit for key '$key'").as(value)
      case None =>
        // Cache miss
        Logger[IO].debug(show"Cache miss for key '$key'") *>
          action.flatTap(t => set(t, key, ttl(t)))
    }
end CacheService
