package httpServer

import models.User
import zio.json.{DeriveJsonDecoder, JsonDecoder}

object Requests {

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
}
