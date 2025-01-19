package models

import zio.json.{DeriveJsonDecoder, JsonDecoder}

import java.util.UUID

// TODO add email validation to constructor
final case class User(
    id: Option[UUID],
    firstName: String,
    lastName: String,
    email: String,
    phoneNumber: String
)

object User {
  implicit val decoder: JsonDecoder[User] = DeriveJsonDecoder.gen[User]
}
