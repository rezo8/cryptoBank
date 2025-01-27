package models

import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

import java.time.Instant
import java.util.UUID

final case class Wallet(
    id: UUID,
    userId: UUID,
    currency: String, // TODO Make this an enum
    balance: Long, // Stored in a low unit level. EX: Satoshi vs Bitcoin
    walletName: String,
    createdAt: Instant,
    updatedAt: Instant
)

object Wallet {
  implicit val decoder: JsonDecoder[Wallet] = DeriveJsonDecoder.gen[Wallet]
  implicit val encoder: JsonEncoder[Wallet] = DeriveJsonEncoder.gen[Wallet]
}
