package tgtg

import cats.effect.IO
import cats.effect.std.Random
import cats.syntax.all.*
import org.legogroup.woof.{Logger, given}
import sttp.client4.*
import sttp.client4.circe.*
import sttp.model.*
import tgtg.cache.CacheService

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.*

class TooGoodToGo(http: Backend[IO])(using log: Logger[IO]):
  private val baseUri          = uri"https://apptoogoodtogo.com/api/"
  private val refreshEndpoint  = uri"${baseUri}auth/v3/token/refresh"
  private val itemsEndpoint    = uri"${baseUri}item/v8/"
  private val loginEndpoint    = uri"${baseUri}auth/v3/authByEmail"
  private val authPollEndpoint = uri"${baseUri}auth/v3/authByRequestPollingId"

  def getItems(cache: CacheService, config: TgtgConfig) =
    def retrieveItems(access: AccessToken) =
      headers()
        .flatMap(ua =>
          basicRequest
            .body(GetItemsRequest(config.userId))
            .post(itemsEndpoint)
            .cookies(access.cookies.toSeq*)
            .headers(ua)
            .auth
            .bearer(access.access_token)
            .response(asJson[GetItemsResponse])
            .responseGetRight
            .send(http)
        )
        .map(_.body.items)
        .flatTap(items => log.info(show"Found ${items.length} items."))
        .logTimed("retrieve items")

    for
      token <- getAccessToken(cache, config)
      items <- retrieveItems(token)
    yield items
  end getItems

  def getAccessToken(cache: CacheService, config: TgtgConfig): IO[AccessToken] =
    val action = headers()
      .flatMap: ua =>
        basicRequest
          .body(RefreshRequest(config.refreshToken))
          .post(refreshEndpoint)
          .response(asJson[RefreshResponse])
          .headers(ua)
          .responseGetRight
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

    cache.retrieveOrSet(action, s"tgtg-accessToken/${config.userId}", a => a.ttl / 4 * 3)
  end getAccessToken

  def getCredentials(email: Email): IO[TgtgConfig] =
    headers().flatMap: ua =>
      basicRequest
        .body(LoginRequest(email))
        .post(loginEndpoint)
        .response(asJson[LoginResponse])
        .headers(ua)
        .responseGetRight
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
                .body(PollRequest(email, pollId))
                .post(authPollEndpoint)
                .response(asJson[Option[PollResponse]])
                .headers(ua)
                .responseGetRight
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
                    case None => IO.sleep(pollSleep) >> poll(triesLeft - 1)
                    case Some(value) =>
                      TgtgConfig(refreshToken = value.refresh_token, userId = value.startup_data.user.user_id).pure
                  }
              log.info(
                "Check your mailbox on PC to continue... (Mailbox on mobile won't work, if you have installed tgtg app.)\n"
              ) *>
                poll(maxPollRetries)
            case StatusCode.TooManyRequests => IO.raiseError(new Exception("Too many requests. Try again later."))
            case _: StatusCode              => IO.raiseError(new Exception(show"Unexpected response: ${r.show()}"))

  private def headers(): IO[Map[String, String]] = Random
    .scalaUtilRandom[IO]
    .flatMap(_.betweenInt(0, userAgents.length))
    .map(i =>
      Map(
        HeaderNames.Accept         -> "application/json",
        HeaderNames.AcceptEncoding -> "gzip",
        HeaderNames.AcceptLanguage -> "en-GB",
        HeaderNames.ContentType    -> "application/json; charset=utf-8",
        HeaderNames.UserAgent      -> userAgents(i)
      )
    )

  private def userAgents = List(
    "TGTG/23.6.11 Dalvik/2.1.0 (Linux; U; Android 9; Nexus 5 Build/M4B30Z)",
    "TGTG/23.6.11 Dalvik/2.1.0 (Linux; U; Android 10; SM-G935F Build/NRD90M)",
    "TGTG/23.6.11 Dalvik/2.1.0 (Linux; Android 12; SM-G920V Build/MMB29K)"
  )

end TooGoodToGo
