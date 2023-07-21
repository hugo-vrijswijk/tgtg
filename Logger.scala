import cats.FlatMap
import cats.effect.{Clock, IO}
import cats.syntax.flatMap.*
import cats.syntax.functor.*
import cats.syntax.show.*
import org.legogroup.woof.{*, given}
import sttp.client4.logging.LogLevel as SttpLogLevel

class LogWrapper[F[_]](using inner: Logger[F]) extends sttp.client4.logging.Logger[F]:
  def apply(level: SttpLogLevel, message: => String): F[Unit] = inner.doLog(matchLevel(level), message)

  private def matchLevel(level: SttpLogLevel): LogLevel = level match
    case SttpLogLevel.Trace => LogLevel.Trace
    case SttpLogLevel.Debug => LogLevel.Debug
    case SttpLogLevel.Info  => LogLevel.Info
    case SttpLogLevel.Warn  => LogLevel.Warn
    case SttpLogLevel.Error => LogLevel.Error

  def apply(level: SttpLogLevel, message: => String, t: Throwable): F[Unit] =
    inner.doLog(matchLevel(level), show"$message ${t.toString()}")

end LogWrapper

def makeLogger: IO[Logger[IO]] =
  given Printer = ColorPrinter()
  given Filter  = Filter.everything
  DefaultLogger.makeIo(Output.fromConsole[IO])

extension [F[_]: FlatMap: Clock, T](fa: F[T])
  def logTimed(msg: String)(using Logger[F], LogInfo): F[T] =
    Clock[F]
      .timed(fa)
      .flatTap((time, _) => Logger[F].debug(show"Completed $msg in $time"))
      .map(_._2)
