package utils

import services.Exceptions.ServerException
import zio.ZIO

object ZioTypes {
  type RezoTask[A] = ZIO[Any, ServerException, A]
}
