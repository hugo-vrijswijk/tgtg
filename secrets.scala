import cats.Functor
import cats.effect.std.Env
import cats.syntax.functor.*

object secrets:
  def refreshToken[F[_]: Env: Functor]: F[String] = loadEnv("TGTG_REFRESH_TOKEN")
  def userId[F[_]: Env: Functor]: F[String]       = loadEnv("TGTG_USER_ID")
  def gotifyKey[F[_]: Env: Functor]: F[String]    = loadEnv("GOTIFY_KEY")
  def cronitor[F[_]: Env: Functor]: F[String]     = loadEnv("CRONITOR_KEY")

  private def loadEnv[F[_]: Env: Functor](name: String) = Env[F].get(name).map(_.get)
end secrets
