package models

import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

import java.util.UUID

final case class AccountCoin(
    accountCoinId: UUID,
    coinId: UUID,
    accountId: UUID,
    satoshis: Long
)

object AccountCoin {
  implicit val decoder: JsonDecoder[AccountCoin] =
    DeriveJsonDecoder.gen[AccountCoin]
  implicit val encoder: JsonEncoder[AccountCoin] =
    DeriveJsonEncoder.gen[AccountCoin]
}
