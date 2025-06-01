package tgtg

import cats.effect.IO
import cats.effect.std.{Random, UUIDGen}
import cats.syntax.all.*
import org.legogroup.woof.{Logger, given}
import sttp.client4.*
import sttp.client4.circe.*
import sttp.model.*
import tgtg.cache.{CacheKey, CacheService}

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.*

class TooGoodToGo(http: Backend[IO])(using log: Logger[IO]):
  private val baseUri          = uri"https://apptoogoodtogo.com/api/"
  private val refreshEndpoint  = uri"${baseUri}token/v1/refresh"
  private val itemsEndpoint    = uri"${baseUri}item/v8/"
  private val loginEndpoint    = uri"${baseUri}auth/v5/authByEmail"
  private val authPollEndpoint = uri"${baseUri}auth/v5/authByRequestPollingId"

  def getItems(cache: CacheService, config: TgtgConfig) =
    def retrieveItems(access: AccessToken) =
      headers(true)
        .flatMap(baseHeaders =>
          basicRequest
            .body(asJson(GetItemsRequest()))
            .post(itemsEndpoint)
            .cookies(access.cookies.toSeq*)
            .headers(baseHeaders*)
            .auth
            .bearer(access.access_token)
            .response(asJsonOrFail[GetItemsResponse])
            .send(http)
        )
        .map(_.body.items)
        .flatTap(items => log.debug(show"Found ${items.length} stores."))
        .logTimed("retrieve stores")

    getAccessToken(cache, config).flatMap(retrieveItems)
  end getItems

  def getAccessToken(cache: CacheService, config: TgtgConfig): IO[AccessToken] =
    val action = headers(true)
      .flatMap: baseHeaders =>
        basicRequest
          .body(asJson(RefreshRequest(config.refreshToken)))
          .post(refreshEndpoint)
          .response(asJsonOrFail[RefreshResponse])
          .headers(baseHeaders*)
          .send(http)
      .map(r =>
        AccessToken(
          r.cookies
            .collect:
              case Right(c) => c
            .map(c => (c.name, c.value)),
          r.body.access_token,
          ttl = FiniteDuration(r.body.access_token_ttl_seconds, TimeUnit.SECONDS)
        )
      )

    cache.retrieveOrSet(action, CacheKey(s"tgtg/accessToken"), a => a.ttl / 4 * 3)
  end getAccessToken

  def getCredentials(email: Email): IO[TgtgConfig] =
    headers(false).flatMap: baseHeaders =>
      basicRequest
        .body(asJson(LoginRequest(email)))
        .post(loginEndpoint)
        .response(asJsonOrFail[LoginResponse])
        .headers(baseHeaders*)
        .send(http)
        .flatMap: r =>
          r.code match
            case StatusCode.Ok if r.body.state == "TERMS" =>
              IO.raiseError(
                new Exception(
                  show"This email $email is not linked to a tgtg account. Please signup with this email first."
                )
              )
            case StatusCode.Ok if r.body.state == "WAIT" && r.body.polling_id.isDefined =>
              val maxPollRetries = 24
              val pollSleep      = 5.seconds
              val pollId         = r.body.polling_id.get

              val pollRequest: IO[Option[PollResponse]] = basicRequest
                .body(asJson(PollRequest(email, pollId)))
                .post(authPollEndpoint)
                .response(asJsonOrFail[Option[PollResponse]])
                .headers(baseHeaders*)
                .send(http)
                .logTimed("poll")
                .flatMap: response =>
                  if response.code == StatusCode.Accepted then IO.none
                  else response.body.pure

              def poll(triesLeft: Int): IO[TgtgConfig] =
                if triesLeft <= 0 then
                  IO.raiseError(new Exception(show"Max retries (${maxPollRetries * pollSleep}) reached. Try again."))
                else
                  // Poll, or sleep and try again
                  pollRequest.flatMap {
                    case None        => IO.sleep(pollSleep) >> poll(triesLeft - 1)
                    case Some(value) =>
                      TgtgConfig(refreshToken = value.refresh_token).pure
                  }
              log.info(
                "Check your mailbox on PC to continue... (Mailbox on mobile won't work, if you have installed tgtg app.)\n"
              ) *>
                poll(maxPollRetries)
            case StatusCode.TooManyRequests => IO.raiseError(new Exception("Too many requests. Try again later."))
            case _: StatusCode              => IO.raiseError(new Exception(show"Unexpected response: ${r.show()}"))

  private def headers(addCorrelationId: Boolean): IO[Seq[Header]] =
    (
      Random
        .scalaUtilRandom[IO]
        .flatMap(_.betweenInt(0, userAgents.length)),
      UUIDGen.randomString[IO]
    ).parMapN: (i, correlationId) =>
      Seq(
        Header.accept(MediaType.ApplicationJson),
        Header.acceptEncoding("gzip"),
        Header(HeaderNames.AcceptLanguage, "en-GB"),
        Header.userAgent(userAgents(i))
      ) ++ (if addCorrelationId then Seq(Header("x-correlation-id", correlationId)) else Seq.empty)

  private def userAgents = List(
    "TGTG/24.11.0 Dalvik/2.1.0 (Linux; U; Android 9; Nexus 5 Build/M4B30Z)",
    "TGTG/24.11.0 Dalvik/2.1.0 (Linux; U; Android 10; SM-G935F Build/NRD90M)",
    "TGTG/24.11.0 Dalvik/2.1.0 (Linux; Android 12; SM-G920V Build/MMB29K)"
  )

end TooGoodToGo
