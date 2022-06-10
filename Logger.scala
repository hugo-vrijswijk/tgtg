import cats.FlatMap
import cats.effect.{Clock, IO}
import cats.syntax.flatMap.*
import cats.syntax.functor.*
import cats.syntax.show.*
import org.legogroup.woof.{*, given}
import sttp.client3.logging.LogLevel as SttpLogLevel

import scala.concurrent.duration.FiniteDuration

class LogWrapper(using inner: Logger[IO]) extends sttp.client3.logging.Logger[IO]:
  def apply(level: SttpLogLevel, message: => String): IO[Unit] = inner.doLog(matchLevel(level), message)

  private def matchLevel(level: SttpLogLevel): LogLevel = level match
    case SttpLogLevel.Trace => LogLevel.Trace
    case SttpLogLevel.Debug => LogLevel.Debug
    case SttpLogLevel.Info  => LogLevel.Info
    case SttpLogLevel.Warn  => LogLevel.Warn
    case SttpLogLevel.Error => LogLevel.Error

  def apply(level: SttpLogLevel, message: => String, t: Throwable): IO[Unit] =
    inner.doLog(matchLevel(level), show"$message ${t.toString()}")

end LogWrapper

def makeLogger: IO[Logger[IO]] =
  given Printer = ColorPrinter()
  given Filter  = Filter.everything
  DefaultLogger.makeIo(Output.fromConsole[IO])

extension [F[_]: FlatMap: Clock, T](io: F[T])
  def logTimed(msg: String)(using Logger[F], LogInfo): F[T] =
    Clock[F]
      .timed(io)
      .flatTap((time, r) => Logger[F].debug(show"Completed $msg in $time"))
      .map(_._2)
