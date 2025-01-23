package models

import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

case class WalletWithCoins(wallet: Wallet, coins: List[WalletCoin])

object WalletWithCoins {
  implicit val decoder: JsonDecoder[WalletWithCoins] =
    DeriveJsonDecoder.gen[WalletWithCoins]
  implicit val encoder: JsonEncoder[WalletWithCoins] =
    DeriveJsonEncoder.gen[WalletWithCoins]
}
