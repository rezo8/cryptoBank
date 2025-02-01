package utils

import repository.Exceptions.RepositoryException
import services.Exceptions.ServerException
import zio.ZIO

object ZioTypes {
  type RezoTask[A] = ZIO[Any, ServerException, A]
  type RezoDBTask[A] = ZIO[Any, RepositoryException, A]
}
