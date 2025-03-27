package tgtg

import cats.data.NonEmptyList
import cats.syntax.all.*
import com.comcast.ip4s.{Host, *}
import com.monovore.decline.{Argument, Opts, Visibility}
import cron4s.{Cron, CronExpr}
import org.legogroup.woof.LogLevel
import sttp.model.Uri
import tgtg.notification.NotifyConfig

import scala.concurrent.duration.*

object UserId extends NewType[String]
type UserId = UserId.Type

object ApiToken extends NewType[String]
type ApiToken = ApiToken.Type

case class TgtgConfig(refreshToken: ApiToken)
case class RedisConfig(host: Host)
case class ServerConfig(
    intervals: Option[NonEmptyList[FiniteDuration]],
    crons: Option[NonEmptyList[CronExpr]],
    isServer: Boolean
)

object Email extends NewType[String]
type Email = Email.Type

trait BaseConfig:
  def log: LogLevel

case class AuthConfig(email: Option[Email], log: LogLevel) extends BaseConfig

case class Config(
    tgtg: TgtgConfig,
    notification: NotifyConfig,
    cronitor: Option[ApiToken],
    redis: Option[RedisConfig],
    server: ServerConfig,
    log: LogLevel
) extends BaseConfig

object Config:

  import allOpts.*
  val auth =
    Opts.subcommand("auth", "Authenticate with TooGoodToGo to retrieve your credentials (user-id and refresh-token).")(
      (userEmail, log).mapN(AuthConfig.apply)
    )
  val opts = (tgtg, notification, cronitor, redis, server, log).mapN(Config.apply)

  object allOpts:
    private given Argument[Uri] = Argument.from("url")(
      Uri
        .parse(_)
        .ensureOr(s => show"Url $s must have a scheme (http:// or https://)")(_.scheme.isDefined)
        .ensureOr(s => show"Url $s must have a host")(_.host.isDefined)
        .ensureOr(s => show"Url $s must not be relative")(_.isAbsolute)
        .toValidatedNel
    )
    private given Argument[Host] =
      Argument.from("host")(str => Host.fromString(str).toRight(show"Invalid host $str").toValidatedNel)

    private given Argument[LogLevel] = Argument.from("level")(_.toLowerCase match
      case "debug" => LogLevel.Debug.validNel
      case "info"  => LogLevel.Info.validNel
      case "warn"  => LogLevel.Warn.validNel
      case "error" => LogLevel.Error.validNel
      case other   => show"Invalid log level $other. Should be one of 'debug', 'info', 'warn' or 'error'".invalidNel)

    private given Argument[ApiToken] = Argument.from("token")(ApiToken(_).validNel)
    private given Argument[Email]    = Argument.from("email")(Email(_).validNel)

    given Argument[CronExpr] = Argument.from("cron")(c =>
      Cron
        .parse(c)
        .leftMap(e => show"Invalid cron '$c': $e")
        .toValidatedNel
    )

    private val refreshHelp =
      "Refresh token for TooGoodToGo. Get it using the `tgtg auth` command."
    private val refreshToken =
      Opts.option[ApiToken]("refresh-token", refreshHelp, "r") orElse
        Opts.env[ApiToken]("TGTG_REFRESH_TOKEN", refreshHelp)

    private val gotifyHelp = "Gotify token for notifications (optional)."
    private val gotifyToken =
      Opts.option[ApiToken]("gotify-token", gotifyHelp) orElse Opts.env[ApiToken]("GOTIFY_TOKEN", gotifyHelp)
    private val gotifyUrlHelp = "Gotify server URL to send notifications to."
    private val gotifyUrl =
      Opts.option[Uri]("gotify-url", gotifyUrlHelp) orElse Opts.env[Uri]("GOTIFY_URL", gotifyUrlHelp)

    private val gotify = (gotifyToken, gotifyUrl).mapN(NotifyConfig.Gotify.apply)

    private val pushbulletHelp = "Pushbullet token for notifications."
    private val pushbullet =
      (Opts.option[ApiToken]("pushbullet-token", pushbulletHelp) orElse
        Opts.env[ApiToken]("PUSHBULLET_TOKEN", pushbulletHelp))
        .map(NotifyConfig.Pushbullet.apply)

    private val pushoverTokenHelp = "Pushover token for notifications."
    private val pushoverToken =
      Opts.option[ApiToken]("pushover-token", pushoverTokenHelp) orElse
        Opts.env[ApiToken]("PUSHOVER_TOKEN", pushoverTokenHelp)
    private val pushoverUserHelp = "Pushover user ID for notifications."
    private val pushoverUser =
      Opts.option[UserId]("pushover-user", pushoverUserHelp) orElse Opts.env[UserId]("PUSHOVER_USER", pushoverUserHelp)

    private val pushover = (pushoverToken, pushoverUser).mapN(NotifyConfig.Pushover.apply)

    private val webhookHelp = "Webhook URL to send notifications to."
    private val webhook = (Opts.option[Uri]("webhook-url", webhookHelp) orElse
      Opts.env[Uri]("WEBHOOK_URL", webhookHelp)).map(NotifyConfig.Webhook.apply)

    val notification: Opts[NotifyConfig] = List(gotify, pushbullet, pushover, webhook).reduce(_ orElse _)

    private val cronitorHelp = "Cronitor token for monitoring (optional)."
    val cronitor = (Opts.option[ApiToken]("cronitor-token", cronitorHelp) orElse
      Opts.env[ApiToken]("CRONITOR_TOKEN", cronitorHelp)).orNone

    val tgtg = refreshToken.map(TgtgConfig.apply)

    private val redisHostHelp =
      "Specify the Redis host for storing authentication tokens and notification history cache. If not set a local cache.json file will be used instead."
    private val redisHost =
      Opts.option[Host]("redis-host", redisHostHelp, "R") orElse
        Opts.env[Host]("REDIS_HOST", redisHostHelp)

    val redis = redisHost.map(RedisConfig.apply).orNone

    val log = (
      Opts.flag("verbose", "Enable verbose logging for more detailed output.", "v").as(LogLevel.Trace) orElse
        Opts.flag("quiet", "Suppress logging output (only log errors).", "q").as(LogLevel.Error) orElse
        Opts.env[LogLevel]("LOG_LEVEL", "Set the log level (debug, info, warn, error).", "log_level")
    ).withDefault(LogLevel.Info)

    private val intervals =
      Opts.options[FiniteDuration]("interval", "Time interval between checks for available boxes.", "i").orNone

    private val crons =
      Opts.options[CronExpr]("cron", "Cron expression for when to check for available boxes.", "c").orNone

    private val isServer =
      Opts.flag("server", "DEPRECATED. Use --interval or --cron options", "s", Visibility.Partial).orFalse

    val server = (intervals, crons, isServer).mapN(ServerConfig.apply)

    val userEmail = Opts.option[Email]("user-email", "Email to use for authentication (optional).").orNone
  end allOpts

end Config
