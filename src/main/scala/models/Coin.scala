package models

import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

import java.util.UUID

final case class Coin(coinId: UUID, coinName: String)

object Coin {
  implicit val decoder: JsonDecoder[Coin] = DeriveJsonDecoder.gen[Coin]
  implicit val encoder: JsonEncoder[Coin] = DeriveJsonEncoder.gen[Coin]
}
