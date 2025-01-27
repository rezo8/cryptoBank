package httpServer

import models.{CoinValue, User, Account, AccountCoin}
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

  final case class LoadAccountResponse(
      accounts: List[Account]
  ) extends ServerResponse

  object LoadAccountResponse {
    implicit val encoder: JsonEncoder[LoadAccountResponse] =
      DeriveJsonEncoder.gen[LoadAccountResponse]
  }

  final case class CreateAccountResponse(
      accountId: UUID
  ) extends ServerResponse

  object CreateAccountResponse {
    implicit val encoder: JsonEncoder[CreateAccountResponse] =
      DeriveJsonEncoder.gen[CreateAccountResponse]
  }

  final case class CreateCoinResponse(coinId: UUID, coinName: String)
      extends ServerResponse

  object CreateCoinResponse {
    implicit val encoder: JsonEncoder[CreateCoinResponse] =
      DeriveJsonEncoder.gen[CreateCoinResponse]
  }

  final case class LoadUserWithCoinsResponse(
      userId: UUID,
      accountCoins: List[AccountCoin]
  ) extends ServerResponse

  object LoadUserWithCoinsResponse {
    implicit val encoder: JsonEncoder[LoadUserWithCoinsResponse] =
      DeriveJsonEncoder.gen[LoadUserWithCoinsResponse]
  }

  final case class AccountCoinsResponse(
      accountCoins: List[AccountCoin]
  ) extends ServerResponse

  object AccountCoinsResponse {
    implicit val encoder: JsonEncoder[AccountCoinsResponse] =
      DeriveJsonEncoder.gen[AccountCoinsResponse]
  }

  final case class AddCoinToAccountResponse(
      accountCoinId: UUID
  ) extends ServerResponse

  object AddCoinToAccountResponse {
    implicit val encoder: JsonEncoder[AddCoinToAccountResponse] =
      DeriveJsonEncoder.gen[AddCoinToAccountResponse]
  }
}
