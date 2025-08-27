package tgtg

import cats.effect.kernel.Resource
import cats.effect.{Clock, ExitCode, IO}
import cats.syntax.all.*
import org.legogroup.woof.*
import sttp.client4.logging.LogLevel as SttpLogLevel

import java.io.{PrintWriter, StringWriter}

class LogWrapper(using inner: Logger[IO]) extends sttp.client4.logging.Logger[IO]:
  import org.legogroup.woof.given

  override def apply(
      level: SttpLogLevel,
      message: => String,
      throwable: Option[Throwable],
      context: Map[String, Any]
  ): IO[Unit] = inner.doLog(matchLevel(level), throwable.fold(message)(t => show"$message ${t.toString()}"))

  private def matchLevel(level: SttpLogLevel): LogLevel = level match
    case SttpLogLevel.Trace => LogLevel.Trace
    case SttpLogLevel.Debug => LogLevel.Debug
    case SttpLogLevel.Info  => LogLevel.Info
    case SttpLogLevel.Warn  => LogLevel.Warn
    case SttpLogLevel.Error => LogLevel.Error

end LogWrapper

extension [T](fa: IO[T])
  def logTimed(msg: String)(using Logger[IO], LogInfo): IO[T] =
    Clock[IO]
      .timed(fa)
      .flatTap((time, _) => Logger[IO].debug(show"Completed $msg in $time"))
      .map(_._2)

  def handleErrorWithLog(using log: Logger[IO], info: LogInfo): IO[T | ExitCode] =
    fa.handleErrorWith(t =>
      debugLogStacktrace(t) *>
        Logger[IO].error(t.getMessage()) *>
        IO.whenA(t.getCause() != null)(Logger[IO].error(show"Caused by: ${t.getCause().toString()}"))
          .as(ExitCode.Error)
    )

end extension

private def debugLogStacktrace(t: Throwable)(using Logger[IO], LogInfo) =
  Resource
    .fromAutoCloseable(IO(new StringWriter()))
    .flatMap(sw => Resource.fromAutoCloseable(IO(new PrintWriter(sw))).tupleLeft(sw))
    .use: (sw, pw) =>
      IO(t.printStackTrace(pw)) *> IO(sw.toString()).flatMap(Logger[IO].debug(_))
