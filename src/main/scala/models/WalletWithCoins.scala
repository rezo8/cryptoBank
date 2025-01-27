package models

import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

case class AccountWithCoins(account: Account, coins: List[AccountCoin])

object AccountWithCoins {
  implicit val decoder: JsonDecoder[AccountWithCoins] =
    DeriveJsonDecoder.gen[AccountWithCoins]
  implicit val encoder: JsonEncoder[AccountWithCoins] =
    DeriveJsonEncoder.gen[AccountWithCoins]
}
