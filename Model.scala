package tgtg

import cats.Show
import cats.effect.IO
import cats.syntax.all.*
import fs2.io.file.{Flags, Path}
import io.circe.{Codec, Decoder, Encoder}

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration

case class GetItemsRequest(
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

case class Origin(latitude: Long = 0L, longitude: Long = 0L) derives Encoder.AsObject

case class RefreshRequest(refresh_token: ApiToken) derives Encoder.AsObject
case class RefreshResponse(
    access_token: ApiToken,
    refresh_token: ApiToken,
    access_token_ttl_seconds: Long
) derives Decoder

type Cookies = Seq[(String, String)]
case class AccessToken(cookies: Cookies, access_token: ApiToken, ttl: FiniteDuration) derives Codec.AsObject

case class GetItemsResponse(items: Seq[ItemWrapper]) derives Decoder

case class Price(code: String, minor_units: Int, decimals: Int) derives Decoder

given Show[Price] = Show.show: price =>
  val currencySymbol = price.code match
    case "EUR" => "€"
    case "USD" => "$"
    case "GBP" => "£"
    case _     => price.code
  show"$currencySymbol ${price.minor_units / math.pow(10, price.decimals)}"

case class ItemWrapper(item: Item, store: Store, items_available: Int, display_name: String) derives Decoder

given Show[ItemWrapper] = i =>
  show"${i.store.store_name} has ${if i.item.name.isBlank() then "box" else i.item.name} available for ${i.item.item_price}"

case class Item(name: String, item_price: Price) derives Decoder

case class Store(store_name: String) derives Decoder

case class LoginRequest(email: Email, device_type: String = "ANDROID") derives Encoder.AsObject
case class LoginResponse(state: String, polling_id: Option[String]) derives Decoder

case class PollRequest(email: Email, request_polling_id: String, device_type: String = "ANDROID")
    derives Encoder.AsObject

case class PollResponse(refresh_token: ApiToken) derives Decoder

given Codec[FiniteDuration] =
  Codec.from(
    Decoder.decodeLong.map(FiniteDuration(_, TimeUnit.MILLISECONDS)),
    Encoder.encodeLong.contramap(_.toMillis)
  )

extension [T: Encoder](data: T)
  def writeToFile(path: Path, flags: Flags = Flags.Write): IO[Unit] =
    import io.circe.syntax.*
    import fs2.Stream
    import fs2.io.file.Files
    Stream
      .emit(data.asJson.spaces2)
      .through(fs2.text.utf8.encode)
      .through(Files[IO].writeAll(path, flags))
      .compile
      .drain
