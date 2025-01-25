package httpServer

import models.{CoinValue, User, UserType}
import org.mindrot.jbcrypt.BCrypt
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

  // TODO send the password hash into the request, not the raw pw.
  final case class CreateUserRequest(
      userType: String,
      firstName: String,
      lastName: String,
      email: String,
      phoneNumber: String,
      password: String
  )

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

  final case class CreateCoinRequest(coinId: UUID, coinName: String)

  object CreateCoinRequest {
    implicit val decoder: JsonDecoder[CreateCoinRequest] =
      DeriveJsonDecoder.gen[CreateCoinRequest]
  }

  final case class AddCoinToWalletRequest(satoshis: Long)

  object AddCoinToWalletRequest {
    implicit val decoder: JsonDecoder[AddCoinToWalletRequest] =
      DeriveJsonDecoder.gen[AddCoinToWalletRequest]
  }

  final case class UpdateCoinAmountRequest(satoshis: Long)

  object UpdateCoinAmountRequest {
    implicit val decoder: JsonDecoder[UpdateCoinAmountRequest] =
      DeriveJsonDecoder.gen[UpdateCoinAmountRequest]
  }
}
