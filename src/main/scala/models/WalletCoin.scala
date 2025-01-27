package models

import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

import java.util.UUID

final case class WalletCoin(
    walletCoinId: UUID,
    coinId: UUID,
    walletId: UUID,
    satoshis: Long
)

object WalletCoin {
  implicit val decoder: JsonDecoder[WalletCoin] =
    DeriveJsonDecoder.gen[WalletCoin]
  implicit val encoder: JsonEncoder[WalletCoin] =
    DeriveJsonEncoder.gen[WalletCoin]
}
