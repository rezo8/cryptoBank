package models

import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

import java.sql.Timestamp
import java.time.Instant
import java.util.UUID

// TODO add email validation to constructor
final case class User(
    id: Option[UUID],
    userTypeId: Int,
    firstName: String,
    lastName: String,
    email: String,
    phoneNumber: String,
    passwordHash: String,
    createdAt: Instant,
    updatedAt: Instant
) {
  // TODO add invalid user type exception
  def getUserType: UserType =
    UserType.IdToUserTypeMap.getOrElse(this.userTypeId, throw new Exception())
}

object User {
  implicit val decoder: JsonDecoder[User] = DeriveJsonDecoder.gen[User]
  implicit val encoder: JsonEncoder[User] = DeriveJsonEncoder.gen[User]
}

final case class UserType(
    userTypeId: Int,
    typeName: String,
    description: String
)

object UserType {
  // TODO reconsider holding this locally... Maybe enum would work instead
  final val ADMIN =
    UserType(1, "ADMIN", "System administrators with full access.")
  final val CUSTOMER =
    UserType(2, "CUSTOMER", "Regular users who can send and receive funds.")
  final val MERCHANT =
    UserType(3, "MERCHANT", "Businesses that accept payments.")

  final val IdToUserTypeMap = Map(
    1 -> ADMIN,
    2 -> CUSTOMER,
    3 -> MERCHANT
  )

  def intFromString(userType: String): Int = {
    (userType.toLowerCase match {
      case "admin"    => ADMIN
      case "merchant" => MERCHANT
      case _          => CUSTOMER
    }).userTypeId
  }
}
