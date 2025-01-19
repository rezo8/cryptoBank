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
      s"User with email [$uuid] does not exist."

    override def status: Status.Error = Status.NotFound
  }
  case class UserAlreadyExists() extends ServerException {
    override def getMessage: String =
      "User already exists. Please change email or phone number"
    override def status: Status.Error = Status.Conflict
  }

  case class Unexpected() extends ServerException {
    override def getMessage: String =
      "Unexpected Exception"
  }
}
