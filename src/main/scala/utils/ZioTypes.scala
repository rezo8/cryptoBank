package utils

import repository.Exceptions.ServerException
import zio.ZIO

object ZioTypes {
  type RezoTask[A] = ZIO[Any, ServerException, A]
}
