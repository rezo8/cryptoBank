package models

import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

import java.util.UUID

final case class UserWithCoins(
    userId: UUID,
    walletCoins: List[WalletCoin]
)

object UserWithCoins {
  implicit val decoder: JsonDecoder[UserWithCoins] =
    DeriveJsonDecoder.gen[UserWithCoins]
  implicit val encoder: JsonEncoder[UserWithCoins] =
    DeriveJsonEncoder.gen[UserWithCoins]
}
