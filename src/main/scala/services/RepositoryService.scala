package services

import repository.Exceptions.{
  MissingEntry,
  RepositoryException,
  UnexpectedSqlError,
  UniqueViolation
}
import services.Exceptions.{
  DatabaseConflict,
  MissingDatabaseObject,
  ServerException,
  Unexpected
}

trait RepositoryService {
  def handleRepositoryExceptions(
      repoException: RepositoryException
  ): ServerException = {
    repoException match {
      case e: UniqueViolation    => DatabaseConflict(e.getMessage)
      case e: MissingEntry       => MissingDatabaseObject(e.getMessage)
      case e: UnexpectedSqlError => Unexpected(e)
    }
  }
}
