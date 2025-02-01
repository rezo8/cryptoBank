package services

import zio.http.Status

object Exceptions {

  trait ServerException extends Exception {
    def status: Status.Error = Status.InternalServerError
  }

  case class DatabaseConflict(message: String) extends ServerException {
    override def getMessage: String = message
    override def status: Status.Error = Status.Conflict
  }

  case class UnexpectedUpdate(message: String) extends ServerException {
    override def getMessage: String =
      s"Unexpected Updates: ${message}"
    override def status: Status.Error = Status.InternalServerError
  }

  case class MissingDatabaseObject(message: String) extends ServerException {
    override def getMessage: String = message
    override def status: Status.Error = Status.NotFound
  }

  case class Unexpected(exception: Throwable) extends ServerException {
    override def getMessage: String =
      s"Unexpected Exception: ${exception.getMessage}"
    override def status: Status.Error = Status.InternalServerError
  }

  case class UnparseableRequest(badField: String) extends ServerException {
    override def getMessage: String =
      s"Bad Request with error on field: ${badField}"

    override def status: Status.Error = Status.BadRequest
  }
}
