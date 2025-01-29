package repository

import zio.http.Status

import java.util.UUID

object Exceptions {

  trait ServerException extends Exception {
    def status: Status.Error = Status.InternalServerError
  }

  case class UserIsMissingByEmail(email: String) extends ServerException {
    override def getMessage: String =
      s"User with email [$email] does not exist."

    override def status: Status.Error = Status.NotFound
  }

  case class UserIsMissingByUUID(uuid: UUID) extends ServerException {
    override def getMessage: String =
      s"User with userId [$uuid] does not exist."

    override def status: Status.Error = Status.NotFound
  }

  case class UserAlreadyExists() extends ServerException {
    override def getMessage: String =
      "User already exists. Please change email or phone number"
    override def status: Status.Error = Status.Conflict
  }

  case class AccountAlreadyExists(userId: UUID) extends ServerException {
    override def getMessage: String =
      s"Account already exists for user [${userId.toString}]. Please use their existing account."

    override def status: Status.Error = Status.Conflict
  }

  case class AccountIsMissingByUserId(userId: UUID) extends ServerException {
    override def getMessage: String =
      s"Account with userId [$userId] does not exist."

    override def status: Status.Error = Status.NotFound
  }

  case class AccountIsMissingByAccountId(accountId: UUID)
      extends ServerException {
    override def getMessage: String =
      s"Account with accountId [$accountId] does not exist."

    override def status: Status.Error = Status.NotFound
  }

  // TODO have take in root exception
  case class Unexpected(exception: Throwable) extends ServerException {
    override def getMessage: String =
      s"Unexpected Exception: ${exception.getMessage}"
  }

  case class UnparseableRequest(badField: String) extends ServerException {
    override def getMessage: String =
      s"Bad Request with error on field: ${badField}"

    override def status: Status.Error = Status.BadRequest
  }

  case class AddressIsMissingByAccountUUID(accountId: UUID)
      extends ServerException {
    override def getMessage: String =
      s"Address with accountId [$accountId] does not exist."

    override def status: Status.Error = Status.NotFound
  }

  case class AddressIsMissingByAddressId(addressId: UUID)
      extends ServerException {
    override def getMessage: String =
      s"Address with addressId [$addressId] does not exist."

    override def status: Status.Error = Status.NotFound
  }

}
