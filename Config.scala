import cats.syntax.all.*
import com.comcast.ip4s.Host
import com.monovore.decline.{Argument, Opts}
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

case class Config(tgtg: TgtgConfig, gotify: GotifyConfig, cronitor: Option[CronitorToken])

object Config:

  import allOpts.*
  val opts = (tgtgConfig, gotify, cronitor).mapN(Config.apply)

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

    val redisHostHelp = "Redis host (for storing auth and notification cache)"
    val redisHost = (Opts.option[Host]("redis-host", "Redis host") orElse Opts.env[Host]("REDIS_HOST", "Redis host"))
      .withDefault("localhost")

  end allOpts
end Config
