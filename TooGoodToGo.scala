import cats.effect.IO
import cats.effect.std.Random
import cats.syntax.show.*
import io.circe.Json
import org.legogroup.woof.{Logger, given}
import sttp.client3.*
import sttp.client3.circe.*
import sttp.model.*

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.*

class TooGoodToGo(cache: CacheService)(using HttpBackend, Logger[IO]):
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
            .send(summon)
        )
        .map(_.body.items)

    for
      r     <- getAccessToken
      items <- retrieveItems(r)
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
          .send(summon)
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
        HeaderNames.UserAgent -> userAgents(i)
      )
    )

  private def userAgents = List(
    "TGTG/22.5.5 Dalvik/2.1.0 (Linux; U; Android 6.0.1; Nexus 5 Build/M4B30Z)",
    "TGTG/22.5.5 Dalvik/2.1.0 (Linux; U; Android 7.0; SM-G935F Build/NRD90M)",
    "TGTG/22.5.5 Dalvik/2.1.0 (Linux; Android 6.0.1; SM-G920V Build/MMB29K)"
  )

end TooGoodToGo
