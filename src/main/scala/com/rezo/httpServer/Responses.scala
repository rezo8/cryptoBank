package com.rezo.httpServer

import com.rezo.models.{Account, Address, User}
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
      id: UUID,
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

  final case class CreateAddressResponse(addressId: UUID) extends ServerResponse

  object CreateAddressResponse {
    implicit val encoder: JsonEncoder[CreateAddressResponse] =
      DeriveJsonEncoder.gen[CreateAddressResponse]
  }
  final case class LoadAddressesForAccountResponse(
      addresses: List[Address]
  ) extends ServerResponse

  object LoadAddressesForAccountResponse {
    implicit val encoder: JsonEncoder[LoadAddressesForAccountResponse] =
      DeriveJsonEncoder.gen[LoadAddressesForAccountResponse]
  }
}
