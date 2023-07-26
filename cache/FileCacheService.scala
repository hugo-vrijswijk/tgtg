package tgtg.cache

import cats.Functor
import cats.effect.kernel.Clock
import cats.effect.{IO, Ref, Resource}
import cats.syntax.all.*
import fs2.Stream
import fs2.io.file.{Files, Flags, Path}
import io.circe.parser.decode
import io.circe.syntax.*
import io.circe.{Decoder, Encoder, Json}
import org.legogroup.woof.{Logger, given}
import tgtg.cache.FileCacheService.Cache

import java.time.Instant
import scala.concurrent.duration.FiniteDuration

class FileCacheService(cache: Ref[IO, Cache])(using log: Logger[IO]) extends CacheService:

  override def get[T: Decoder](key: CacheKey): IO[Option[T]] = for
    now  <- Clock[IO].instant
    data <- cache.get.map(_.get(key))
  yield data.mapFilter((ttl, data) => if ttl.isAfter(now) then data.as[T].toOption else none)

  override def set[T: Encoder](value: T, key: CacheKey, ttl: FiniteDuration): IO[Unit] = for
    now <- Clock[IO].instant
    _   <- cache.update(_.updated(key, (now.plusMillis(ttl.toMillis), value.asJson)))
  yield ()

end FileCacheService

object FileCacheService:
  type Cache = Map[CacheKey, (Instant, Json)]

  def create(using log: Logger[IO]): Resource[IO, CacheService] =
    for
      now   <- Clock[IO].instant.toResource
      cache <- readCacheData(now).toResource
      ref <- Resource.make(Ref[IO].of(cache))(ref =>
        for
          now  <- Clock[IO].instant
          data <- ref.get
          updatedData = data.filter(filterExpired(now))

          _ <- IO.whenA(cache != updatedData)(
            writeCacheData(updatedData) *> log.info(
              show"Cache written to disk (cache.json). Cache contains ${updatedData.size} entries."
            )
          )
        yield ()
      )
    yield FileCacheService(ref)

  private def filterExpired(now: Instant) = (entry: (Any, (Instant, Any))) =>
    val (ttl, _) = entry._2
    ttl.isAfter(now)

  private def readCacheData(now: Instant): IO[Cache] = Files[IO]
    .readAll(Path("cache.json"))
    .through(fs2.text.utf8.decode)
    .map(decode[Cache])
    .flatMap(fs2.Stream.fromEither[IO](_))
    .handleError(_ => Map.empty)
    .map(_.filter(filterExpired(now)))
    .head
    .compile
    .lastOrError

  private def writeCacheData(data: Cache): IO[Unit] = Stream
    .emit(data.asJson.spaces2)
    .through(fs2.text.utf8.encode)
    .through(Files[IO].writeAll(Path("cache.json"), Flags.Write))
    .compile
    .drain

end FileCacheService

// Copied from IO.realtimeInstant, which is not available in scala-js
extension [F[_]: Functor](clock: Clock[F])
  def instant: F[Instant] =
    clock.applicative.map(clock.realTime)(d => Instant.EPOCH.plusNanos(d.toNanos))
