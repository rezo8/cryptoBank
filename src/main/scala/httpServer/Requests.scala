package httpServer

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

  final case class CreateAccountRequest(
      userId: UUID,
      cryptoType: String, // TODO make this an enum
      accountName: String
  )

  object CreateAccountRequest {
    implicit val decoder: JsonDecoder[CreateAccountRequest] =
      DeriveJsonDecoder.gen[CreateAccountRequest]
  }

  final case class CreateCoinRequest(coinId: UUID, coinName: String)

  object CreateCoinRequest {
    implicit val decoder: JsonDecoder[CreateCoinRequest] =
      DeriveJsonDecoder.gen[CreateCoinRequest]
  }

  final case class AddCoinToAccountRequest(satoshis: Long)

  object AddCoinToAccountRequest {
    implicit val decoder: JsonDecoder[AddCoinToAccountRequest] =
      DeriveJsonDecoder.gen[AddCoinToAccountRequest]
  }

  final case class UpdateCoinAmountRequest(satoshis: Long)

  object UpdateCoinAmountRequest {
    implicit val decoder: JsonDecoder[UpdateCoinAmountRequest] =
      DeriveJsonDecoder.gen[UpdateCoinAmountRequest]
  }
}
