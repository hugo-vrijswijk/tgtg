import cats.syntax.all.*
import com.comcast.ip4s.{Host, *}
import com.monovore.decline.{Argument, Opts}
import org.legogroup.woof.LogLevel
import sttp.model.Uri

opaque type TgtgToken <: String = String
object TgtgToken:
  given Argument[TgtgToken] = Argument.readString

opaque type GotifyToken <: String = String
object GotifyToken:
  given Argument[GotifyToken] = Argument.readString

opaque type CronitorToken <: String = String
object CronitorToken:
  given Argument[CronitorToken] = Argument.readString

case class TgtgConfig(refreshToken: TgtgToken, userId: String)
case class GotifyConfig(token: GotifyToken, url: Uri)
case class RedisConfig(host: Host)

case class Config(
    tgtg: TgtgConfig,
    gotify: GotifyConfig,
    cronitor: Option[CronitorToken],
    redis: Option[RedisConfig],
    log: LogLevel
)

object Config:

  import allOpts.*
  val opts = (tgtgConfig, gotify, cronitor, redis, log).mapN(Config.apply)

  private object allOpts:
    private given Argument[Uri] = Argument.from("url")(
      Uri
        .parse(_)
        .ensureOr(s => s"Url $s must have a scheme (http:// or https://)")(_.scheme.isDefined)
        .ensureOr(s => s"Url $s must have a host")(_.host.isDefined)
        .ensureOr(s => s"Url $s must not be relative")(_.isAbsolute)
        .toValidatedNel
    )
    private given Argument[Host] =
      Argument.from("host")(str => Host.fromString(str).toRight(s"Invalid host $str").toValidatedNel)

    private given Argument[LogLevel] = Argument.from("level")(_.toLowerCase match
      case "debug" => LogLevel.Debug.validNel
      case "info"  => LogLevel.Info.validNel
      case "warn"  => LogLevel.Warn.validNel
      case "error" => LogLevel.Error.validNel
      case other   => s"Invalid log level $other. Should be one of 'debug', 'info', 'warn' or 'error'".invalidNel
    )

    val refreshHelp =
      "Refresh token for TooGoodToGo. Get it using https://github.com/ahivert/tgtg-python (coming soon in this CLI)"
    val refreshToken =
      Opts.option[TgtgToken]("refresh-token", refreshHelp) orElse Opts.env[TgtgToken]("TGTG_REFRESH_TOKEN", refreshHelp)

    val userIdHelp =
      "User id for TooGoodToGo. Get it using https://github.com/ahivert/tgtg-python (coming soon in this CLI)"
    val userId = Opts.option[String]("user-id", userIdHelp) orElse Opts.env[String]("TGTG_USER_ID", userIdHelp)

    val gotifyHelp = "Gotify token for notifications"
    val gotifyToken =
      Opts.option[GotifyToken]("gotify-token", gotifyHelp) orElse Opts.env[GotifyToken]("GOTIFY_TOKEN", gotifyHelp)
    val gotifyUrlHelp = "Gotify url to send notifications to"
    val gotifyUrl =
      Opts.option[Uri]("gotify-url", gotifyUrlHelp) orElse Opts.env[Uri]("GOTIFY_URL", gotifyUrlHelp)

    val gotify = (gotifyToken, gotifyUrl).mapN(GotifyConfig.apply)

    val cronitorHelp = "Cronitor token for monitoring (optional)"
    val cronitor = (Opts.option[CronitorToken]("cronitor-token", cronitorHelp) orElse Opts
      .env[CronitorToken]("CRONITOR_TOKEN", cronitorHelp)).orNone

    val tgtgConfig = (refreshToken, userId).mapN(TgtgConfig.apply)

    val redisHostHelp = "Redis host (for storing auth and notification cache). Uses a local cache.json file if not set"
    val redisHost = Opts.option[Host]("redis-host", redisHostHelp) orElse Opts.env[Host]("REDIS_HOST", redisHostHelp)

    val redis = redisHost.map(RedisConfig.apply).orNone

    val logHelp = "Set the log level (debug, info, warn, error)"
    val log = (
      Opts.flag("debug", "enable debug logging for more information", "d").as(LogLevel.Debug) orElse
        Opts.option[LogLevel]("log-level", logHelp, "l") orElse Opts.env[LogLevel]("LOG_LEVEL", logHelp)
    ).withDefault(LogLevel.Info)
  end allOpts

end Config
