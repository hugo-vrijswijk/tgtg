package tgtg

import cats.effect.{Async, IO}
import cats.syntax.all.*
import fs2.*
import fs2.io.*
import org.legogroup.woof.{Logger, given}
import tgtg.*

final class Auth(tgtg: TooGoodToGo, config: AuthConfig)(using log: Logger[IO]):
  def run: IO[Unit] =
    for
      email <- config.email match
        case None        => log.info("TooGoodToGo email:") *> readLine
        case Some(value) => value.pure[IO]
      creds <- tgtg.getCredentials(Email(email))
      _ <- log.info(s"user-id: ${creds.userId}") *>
        log.info(s"refresh-token: ${creds.refreshToken}") *>
        log.info(show"Use these credentials as config (arguments or environment variables)")
    yield ()

  // Weird summon is needed for scala-js cross-compilation issues
  def readLine: IO[String] = stdinUtf8[IO](4096)(summon[Async[IO]])
    .repartition(s => Chunk.array(s.split("\n", -1)))
    .filter(_.nonEmpty)
    .head
    .compile
    .lastOrError
    .handleErrorWith(e => IO.raiseError(new RuntimeException("Failed to read line. Use --email option instead", e)))

end Auth
