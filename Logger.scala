package tgtg

import cats.effect.{Clock, ExitCode, IO}
import cats.syntax.all.*
import org.legogroup.woof.{*, given}
import sttp.client4.logging.LogLevel as SttpLogLevel

class LogWrapper(using inner: Logger[IO]) extends sttp.client4.logging.Logger[IO]:
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

extension [T](fa: IO[T])
  def logTimed(msg: String)(using Logger[IO], LogInfo): IO[T] =
    Clock[IO]
      .timed(fa)
      .flatTap((time, _) => Logger[IO].debug(show"Completed $msg in $time"))
      .map(_._2)

  def handleErrorWithLog(using log: Logger[IO], info: LogInfo): IO[T | ExitCode] =
    fa.handleErrorWith(t =>
      Logger[IO].error(t.getMessage()) *>
        IO.whenA(t.getCause() != null)(Logger[IO].error(show"Caused by: ${t.getCause()}"))
          .as(ExitCode.Error)
    )
end extension
