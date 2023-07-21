import FileCacheService.Cache
import cats.effect.{Async, Clock, Ref, Resource}
import cats.syntax.all.*
import cats.{Applicative, Functor, MonadThrow}
import fs2.Stream
import fs2.io.file.{Files, Flags, Path}
import io.circe.parser.decode
import io.circe.syntax.*
import io.circe.{Decoder, Encoder, Json}
import org.legogroup.woof.{Logger, given}

import java.time.Instant
import scala.concurrent.duration.FiniteDuration

class FileCacheService[F[_]: MonadThrow: Clock: Logger](cache: Ref[F, Cache]) extends CacheService[F]:

  override def get[T: Decoder](key: String): F[Option[T]] = for
    now  <- Clock[F].instant
    data <- cache.get.map(_.get(key))
  yield data.mapFilter((ttl, data) => if ttl.isAfter(now) then data.as[T].toOption else none)

  override def set[T: Encoder](value: T, key: String, ttl: FiniteDuration): F[Unit] = for
    now <- Clock[F].instant
    _   <- cache.update(_.updated(key, (now.plusMillis(ttl.toMillis), value.asJson)))
  yield ()

end FileCacheService

object FileCacheService:
  type Cache = Map[String, (Instant, Json)]

  def create[F[_]: Async: Logger]: Resource[F, CacheService[F]] =
    for
      now   <- Resource.eval(Clock[F].instant)
      cache <- Resource.eval[F, Cache](readCacheData(now))
      ref <- Resource.make(Ref[F].of(cache))(ref =>
        for
          now  <- Clock[F].instant
          data <- ref.get
          updatedData = data.filter(filterExpired(now))

          _ <- Applicative[F].whenA(cache != updatedData)(
            writeCacheData(updatedData) *> Logger[F].info(
              show"Cache written to disk (cache.json). Cache contains ${updatedData.size} entries."
            )
          )
        yield ()
      )
    yield new FileCacheService[F](ref)

  private def filterExpired(now: Instant) = (entry: (Any, (Instant, Any))) =>
    val (ttl, _) = entry._2
    ttl.isAfter(now)

  private def readCacheData[F[_]: Async](now: Instant): F[Cache] = Files[F]
    .readAll(Path("cache.json"))
    .through(fs2.text.utf8.decode)
    .map(decode[Cache])
    .flatMap(fs2.Stream.fromEither[F](_))
    .handleError(_ => Map.empty)
    .map(_.filter(filterExpired(now)))
    .head
    .compile
    .lastOrError

  private def writeCacheData[F[_]: Async](data: Cache): F[Unit] = Stream
    .emit(data.asJson.spaces2)
    .through(fs2.text.utf8.encode)
    .through(Files[F].writeAll(Path("cache.json"), Flags.Write))
    .compile
    .drain

end FileCacheService

extension [F[_]: Functor](clock: Clock[F])
  def instant: F[Instant] = clock.realTime.map(d => Instant.ofEpochMilli(d.toMillis))
