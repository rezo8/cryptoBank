package com.rezo.httpServer

import zio.json.{DeriveJsonDecoder, JsonDecoder}

import java.util.UUID

object Requests {
  trait ServerRequest

  final case class LoadUserByEmailRequest(
      email: String
  ) extends ServerRequest
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
  ) extends ServerRequest

  object CreateUserRequest {
    implicit val decoder: JsonDecoder[CreateUserRequest] =
      DeriveJsonDecoder.gen[CreateUserRequest]
  }

  final case class CreateAccountRequest(
      userId: UUID,
      cryptoType: String, // TODO make this an enum
      accountName: String
  ) extends ServerRequest

  object CreateAccountRequest {
    implicit val decoder: JsonDecoder[CreateAccountRequest] =
      DeriveJsonDecoder.gen[CreateAccountRequest]
  }

  final case class CreateAddressRequest(
      accountId: UUID,
      addressLocation: String,
      balance: Long
  ) extends ServerRequest

  object CreateAddressRequest {
    implicit val decoder: JsonDecoder[CreateAddressRequest] =
      DeriveJsonDecoder.gen[CreateAddressRequest]
  }

  final case class UpdateAddressAmountRequest(satoshis: Long)
      extends ServerRequest

  object UpdateAddressAmountRequest {
    implicit val decoder: JsonDecoder[UpdateAddressAmountRequest] =
      DeriveJsonDecoder.gen[UpdateAddressAmountRequest]
  }
}
