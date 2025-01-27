package httpServer

import models.{CoinValue, User, Wallet, WalletCoin}
import zio.json.{DeriveJsonEncoder, JsonEncoder}

import java.util.UUID

object Responses {
  trait ServerResponse

  final case class MessageResponse(
      message: String
  ) extends ServerResponse

  object MessageResponse {
    implicit val encoder: JsonEncoder[MessageResponse] =
      DeriveJsonEncoder.gen[MessageResponse]
  }

  final case class LoadUserResponse(
      id: Option[UUID],
      firstName: String,
      lastName: String,
      email: String,
      phoneNumber: String
  ) extends ServerResponse
  object LoadUserResponse {
    def fromUser(user: User): LoadUserResponse = {
      LoadUserResponse(
        user.userId,
        firstName = user.firstName,
        lastName = user.lastName,
        email = user.email,
        phoneNumber = user.phoneNumber
      )
    }
    implicit val encoder: JsonEncoder[LoadUserResponse] =
      DeriveJsonEncoder.gen[LoadUserResponse]
  }

  final case class LoadWalletResponse(
      wallets: List[Wallet]
  ) extends ServerResponse

  object LoadWalletResponse {
    implicit val encoder: JsonEncoder[LoadWalletResponse] =
      DeriveJsonEncoder.gen[LoadWalletResponse]
  }

  final case class CreateWalletResponse(
      walletId: UUID
  ) extends ServerResponse

  object CreateWalletResponse {
    implicit val encoder: JsonEncoder[CreateWalletResponse] =
      DeriveJsonEncoder.gen[CreateWalletResponse]
  }

  final case class CreateCoinResponse(coinId: UUID, coinName: String)
      extends ServerResponse

  object CreateCoinResponse {
    implicit val encoder: JsonEncoder[CreateCoinResponse] =
      DeriveJsonEncoder.gen[CreateCoinResponse]
  }

  final case class LoadUserWithCoinsResponse(
      userId: UUID,
      walletCoins: List[WalletCoin]
  ) extends ServerResponse

  object LoadUserWithCoinsResponse {
    implicit val encoder: JsonEncoder[LoadUserWithCoinsResponse] =
      DeriveJsonEncoder.gen[LoadUserWithCoinsResponse]
  }

  final case class WalletCoinsResponse(
      walletCoins: List[WalletCoin]
  ) extends ServerResponse

  object WalletCoinsResponse {
    implicit val encoder: JsonEncoder[WalletCoinsResponse] =
      DeriveJsonEncoder.gen[WalletCoinsResponse]
  }

  final case class AddCoinToWalletResponse(
      walletCoinId: UUID
  ) extends ServerResponse

  object AddCoinToWalletResponse {
    implicit val encoder: JsonEncoder[AddCoinToWalletResponse] =
      DeriveJsonEncoder.gen[AddCoinToWalletResponse]
  }
}
