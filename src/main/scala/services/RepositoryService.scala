package services

import repository.Exceptions.{
  ExcessiveUpdate,
  MissingEntry,
  RepositoryException,
  UnexpectedSqlError,
  UniqueViolation
}
import services.Exceptions.{
  DatabaseConflict,
  MissingDatabaseObject,
  ServerException,
  Unexpected,
  UnexpectedUpdate
}

trait RepositoryService {
  def handleRepositoryExceptions(
      repoException: RepositoryException
  ): ServerException = {
    repoException match {
      case e: UniqueViolation    => DatabaseConflict(e.getMessage)
      case e: MissingEntry       => MissingDatabaseObject(e.getMessage)
      case e: ExcessiveUpdate    => UnexpectedUpdate(e.getMessage)
      case e: UnexpectedSqlError => Unexpected(e)
    }
  }
}
