package repository

import zio.http.Status

object Exceptions {

  trait ServerException extends Exception {
    def status: Status.Error = Status.InternalServerError
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
