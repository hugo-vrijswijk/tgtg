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
      basicRequest
        .body(GetItemsRequest(secrets.userId))
        .post(itemsEndpoint)
        .cookies(access.cookies.toSeq*)
        .headers(userAgent())
        .auth
        .bearer(access.access_token)
        .response(asJson[GetItemsResponse])
        .acceptEncoding(Encodings.Gzip)
        .responseGetRight
        .send(summon)
        .map(_.body.items)
    end retrieveItems

    for
      r     <- getAccessToken
      items <- retrieveItems(r)
    yield items
    end for
  end getItems

  def getAccessToken: IO[AccessToken] =
    val action = basicRequest
      .body(RefreshRequest(secrets.refreshToken))
      .post(refreshEndpoint)
      .response(asJson[RefreshResponse])
      .headers(userAgent())
      .responseGetRight
      .acceptEncoding(Encodings.Gzip)
      .logSettings(logResponseBody = Some(true))
      .send(summon)
      .map(r =>
        AccessToken(
          r.cookies.collect { case Right(c) => c }.map(c => (c.name, c.value)).toList,
          r.body.access_token,
          ttl = FiniteDuration(r.body.access_token_ttl_seconds, TimeUnit.SECONDS)
        )
      )

    cache.retrieveOrSet(action, "tgtg-accessToken", a => a.ttl / 4 * 3)
  end getAccessToken

  private def userAgent() = Map(
    HeaderNames.UserAgent -> "TGTG/22.5.5 Dalvik/2.1.0 (Linux; U; Android 7.0; SM-G935F Build/NRD90M)"
  )

end TooGoodToGo
