package tgtg

import cats.effect.{IO, Resource}
import cats.syntax.all.*
import io.chrisdavenport.rediculous.RedisConnection
import org.legogroup.woof.{*, given}
import sttp.client4.Backend
import sttp.client4.logging.{LogConfig, LoggingBackend}
import sttp.model.HeaderNames
import tgtg.cache.*
import tgtg.http.*
import tgtg.notification.*

object Deps:

  def mkLogger(logLevel: LogLevel): IO[Logger[IO]] =
    given Printer = NoPostfixPrinter(ColorPrinter())
    given Filter  = Filter.atLeastLevel(logLevel)
    DefaultLogger.makeIo(Output.fromConsole[IO])

  def mkHttpBackend(config: Option[CronitorToken])(using log: Logger[IO]) =
    httpBackend
      .map(
        LoggingBackend(
          _,
          new LogWrapper,
          LogConfig(sensitiveHeaders =
            HeaderNames.SensitiveHeaders ++ Set("X-Gotify-Key", "x-amz-cf-id", "x-correlation-id")
          )
        )
      )
      .flatTap(b => config.fold(log.debug("Cronitor disabled").toResource)(cronitor(b, _)))

  def mkNotifyService(http: Backend[IO], notify: NotifyConfig)(using Logger[IO]): NotifyService = notify match
    case config: NotifyConfig.Gotify => Gotify(http, config)

  def mkCache(config: Option[RedisConfig])(using log: Logger[IO]): Resource[IO, CacheService] = config match
    case None => log.info("Using cache.json").toResource *> FileCacheService.create
    case Some(config) =>
      log.info("Using Redis cache").toResource *> RedisConnection
        .queued[IO]
        .withHost(config.host)
        .build
        .map(RedisCacheService(_))

end Deps

/** Custom color printer that removes the postfix from the log info.
  */
final class NoPostfixPrinter(inner: Printer) extends Printer:

  class NoPostfixLogInfo(inner: LogInfo) extends LogInfo(inner.enclosingClass, inner.fileName, inner.lineNumber):
    override def postfix: String = ""
    override def prefix: String  = inner.prefix + s":${lineNumber + 1}"

  override def toPrint(
      epochMillis: EpochMillis,
      level: LogLevel,
      info: LogInfo,
      message: String,
      context: List[(String, String)]
  ): String = inner.toPrint(epochMillis, level, NoPostfixLogInfo(info), message, context)
end NoPostfixPrinter
