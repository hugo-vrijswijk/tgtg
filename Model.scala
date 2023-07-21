import cats.syntax.show.*
import io.circe.{Codec, Decoder, Encoder, JsonObject}

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration

case class GetItemsRequest(
    user_id: String,
    origin: Origin = Origin(),
    // radius: Int = 21,
    page: Int = 1,
    // discover: Boolean = false,
    favorites_only: Boolean = true,
    // item_categories: Seq[String] = Seq.empty,
    // diet_categories: Seq[String] = Seq.empty,
    // pickup_earliest: Option[LocalDate] = None,
    // pickup_latest: Option[LocalDate] = None,
    // search_phrase: Option[String] = None,
    with_stock_only: Boolean = false
    // hidden_only: Boolean = false,
    // we_care_only: Boolean = false
) derives Encoder.AsObject

case class Origin(latitude: Long = 0L, longitude: Long = 0L)

case class RefreshRequest(refresh_token: String) derives Encoder.AsObject
case class RefreshResponse(access_token: String, refresh_token: String, access_token_ttl_seconds: Long) derives Decoder

type Cookies = List[(String, String)]
case class AccessToken(cookies: Cookies, access_token: String, ttl: FiniteDuration) derives Codec.AsObject

case class GotifyMessage(message: String, title: String, priority: Int = 8) derives Encoder.AsObject

case class GetItemsResponse(items: Seq[ItemWrapper]) derives Decoder

case class ItemWrapper(item: Item, store: Store, items_available: Int, display_name: String) derives Decoder:
  def show =
    show"${store.store_name} has ${if item.name.isEmpty then "box" else item.name} available for ${item.price_including_taxes.toEuros}"

case class Item(name: String, price_including_taxes: Price)
case class Price(minor_units: Int):
  def toEuros = show"€${minor_units / 100}"

case class Store(store_name: String)

given Codec[FiniteDuration] =
  Codec.from(
    Decoder.decodeLong.map(FiniteDuration(_, TimeUnit.MILLISECONDS)),
    Encoder.encodeLong.contramap(_.toMillis)
  )
