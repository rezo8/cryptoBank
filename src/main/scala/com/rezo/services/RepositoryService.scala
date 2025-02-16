package com.rezo.services

import com.rezo.repository.Exceptions.*
import Exceptions.*

trait RepositoryService {
  def handleRepositoryExceptions(
      repoException: RepositoryException
  ): ServerException = {
    repoException match {
      case e: UniqueViolation     => DatabaseConflict(e.getMessage)
      case e: ForeignKeyViolation => DatabaseConflict(e.getMessage)
      case e: MissingEntry        => MissingDatabaseObject(e.getMessage)
      case e: ExcessiveUpdate     => UnexpectedUpdate(e.getMessage)
      case e: UnexpectedSqlError  => Unexpected(e)
    }
  }
}
