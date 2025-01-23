package httpServer

import models.{CoinValue, User}
import zio.json.{DeriveJsonDecoder, JsonDecoder}

import java.util.UUID

object Requests {

  final case class LoadUserByEmailRequest(
      email: String
  )
  object LoadUserByEmailRequest {
    implicit val decoder: JsonDecoder[LoadUserByEmailRequest] =
      DeriveJsonDecoder.gen[LoadUserByEmailRequest]
  }
  final case class CreateUserRequest(
      firstName: String,
      lastName: String,
      email: String,
      phoneNumber: String
  ) {
    def toUser(): User = {
      User(
        id = None,
        firstName = firstName,
        lastName = lastName,
        email = email,
        phoneNumber = phoneNumber
      )
    }
  }

  object CreateUserRequest {
    implicit val decoder: JsonDecoder[CreateUserRequest] =
      DeriveJsonDecoder.gen[CreateUserRequest]
  }

  final case class CreateWalletRequest(
      userId: UUID,
      walletName: String
  )

  object CreateWalletRequest {
    implicit val decoder: JsonDecoder[CreateWalletRequest] =
      DeriveJsonDecoder.gen[CreateWalletRequest]
  }

  final case class AddCoinToWalletRequest(
      coinId: UUID,
      walletId: UUID,
      satoshis: Long
  )

  object AddCoinToWalletRequest {
    implicit val decoder: JsonDecoder[AddCoinToWalletRequest] =
      DeriveJsonDecoder.gen[AddCoinToWalletRequest]
  }

  final case class UpdateCoinAmountRequest(
      coinId: Int,
      satoshis: Long
  )

  object UpdateCoinAmountRequest {
    implicit val decoder: JsonDecoder[UpdateCoinAmountRequest] =
      DeriveJsonDecoder.gen[UpdateCoinAmountRequest]
  }
}
