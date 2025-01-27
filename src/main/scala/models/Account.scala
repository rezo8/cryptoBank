package models

import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

import java.time.Instant
import java.util.UUID

final case class Account(
    id: UUID,
    userId: UUID,
    cryptoType: String, // TODO Make this an enum
    balance: Long, // Stored in a low unit level. EX: Satoshi vs Bitcoin
    accountName: String,
    createdAt: Instant,
    updatedAt: Instant
)

object Account {
  implicit val decoder: JsonDecoder[Account] = DeriveJsonDecoder.gen[Account]
  implicit val encoder: JsonEncoder[Account] = DeriveJsonEncoder.gen[Account]
}
