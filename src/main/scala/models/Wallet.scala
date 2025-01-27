package models

import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

import java.time.Instant
import java.util.UUID

final case class Wallet(
    id: UUID,
    userId: UUID,
    currency: String, // TODO Make this an enum
    balance: BigDecimal, // This is balance in whole, rather than satoshis.
    walletName: String,
    createdAt: Instant,
    updatedAt: Instant
)

object Wallet {
  implicit val decoder: JsonDecoder[Wallet] = DeriveJsonDecoder.gen[Wallet]
  implicit val encoder: JsonEncoder[Wallet] = DeriveJsonEncoder.gen[Wallet]
}
