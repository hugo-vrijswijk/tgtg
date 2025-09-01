package tgtg

import cats.effect.IO
import cats.syntax.all.*
import fs2.io.*
import fs2.text
import io.github.iltotore.iron.*
import io.github.iltotore.iron.cats.given
import io.github.iltotore.iron.constraint.string.ValidEmail
import org.legogroup.woof.{Logger, given}
import tgtg.*

final class Auth(tgtg: TooGoodToGo, config: AuthConfig)(using log: Logger[IO]):
  def run: IO[Unit] =
    for
      email <- config.email match
        case None        => log.info("TooGoodToGo email:") *> readLine
        case Some(value) => value.pure[IO]
      creds <- tgtg.getCredentials(email)
      _     <- log.info(show"refresh-token: ${creds.refreshToken}") *>
        log.info("Use these credentials as config (arguments or environment variables)")
    yield ()

  // Weird summon is needed for scala-js cross-compilation issues
  def readLine: IO[Email] = stdinUtf8[IO](4096)
    .through(text.lines)
    .map(_.strip())
    .filter(_.nonEmpty)
    .map(_.refineEither[ValidEmail])
    .evalMap:
      case Left(err) => log.error(err).as(none)
      case Right(v)  => IO.pure(Email(v).some)
    .unNone
    .head
    .compile
    .lastOrError
    .handleErrorWith(e =>
      IO.raiseError(new RuntimeException("Failed to read line. Use --user-email option instead", e))
    )

end Auth
