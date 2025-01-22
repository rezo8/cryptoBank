package models

import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

import java.util.UUID

final case class Wallet(id: UUID, ownerId: UUID, walletName: String)

object Wallet {
  implicit val decoder: JsonDecoder[Wallet] = DeriveJsonDecoder.gen[Wallet]
  implicit val encoder: JsonEncoder[Wallet] = DeriveJsonEncoder.gen[Wallet]
}
