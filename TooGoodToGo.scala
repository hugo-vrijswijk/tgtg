import cats.effect.IO
import cats.effect.std.Random
import cats.syntax.show.*
import io.circe.Json
import org.legogroup.woof.{Logger, given}
import sttp.client4.*
import sttp.client4.circe.*
import sttp.model.*

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.*

class TooGoodToGo(cache: CacheService, http: Backend[IO])(using Logger[IO]):
  private val baseUri         = uri"https://apptoogoodtogo.com/api/"
  private val refreshEndpoint = uri"${baseUri}auth/v3/token/refresh"
  private val itemsEndpoint   = uri"${baseUri}item/v7/"

  def getItems =
    def retrieveItems(access: AccessToken) =
      userAgent()
        .flatMap(ua =>
          basicRequest
            .body(GetItemsRequest(secrets.userId))
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
        .flatTap(items => Logger[IO].info(show"Found ${items.length} items."))
        .logTimed("retrieve items")

    for
      token <- getAccessToken
      items <- retrieveItems(token)
    yield items
  end getItems

  def getAccessToken: IO[AccessToken] =
    val action = userAgent()
      .flatMap(ua =>
        basicRequest
          .body(RefreshRequest(secrets.refreshToken))
          .post(refreshEndpoint)
          .response(asJson[RefreshResponse])
          .headers(ua)
          .responseGetRight
          .send(http)
      )
      .map(r =>
        AccessToken(
          r.cookies.collect { case Right(c) => c }.map(c => (c.name, c.value)).toList,
          r.body.access_token,
          ttl = FiniteDuration(r.body.access_token_ttl_seconds, TimeUnit.SECONDS)
        )
      )

    cache.retrieveOrSet(action, "tgtg-accessToken", a => a.ttl / 4 * 3)
  end getAccessToken

  private def userAgent(): IO[Map[String, String]] = Random
    .scalaUtilRandom[IO]
    .flatMap(_.betweenInt(0, userAgents.length))
    .map(i =>
      Map(
        HeaderNames.Accept         -> "application/json",
        HeaderNames.AcceptEncoding -> "gzip",
        HeaderNames.AcceptLanguage -> "en-UK",
        HeaderNames.ContentType    -> "application/json; charset=utf-8",
        HeaderNames.UserAgent      -> userAgents(i)
      )
    )

  private def userAgents = List(
    "TGTG/23.3.11 Dalvik/2.1.0 (Linux; U; Android 9; Nexus 5 Build/M4B30Z)",
    "TGTG/23.3.11 Dalvik/2.1.0 (Linux; U; Android 10; SM-G935F Build/NRD90M)",
    "TGTG/23.3.11 Dalvik/2.1.0 (Linux; Android 12; SM-G920V Build/MMB29K)"
  )

end TooGoodToGo
