package models

import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

import java.time.Instant
import java.util.UUID

case class Address(
    addressId: UUID,
    accountId: UUID,
    address: String,
    balance: Long,
    isActive: Boolean,
    createdAt: Instant,
    updatedAt: Instant
)

object Address {
  implicit val decoder: JsonDecoder[Address] = DeriveJsonDecoder.gen[Address]
  implicit val encoder: JsonEncoder[Address] = DeriveJsonEncoder.gen[Address]
}
