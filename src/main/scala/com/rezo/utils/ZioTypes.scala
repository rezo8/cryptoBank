package com.rezo.utils

import com.rezo.repository.Exceptions.RepositoryException
import com.rezo.services.Exceptions.ServerException
import zio.ZIO

object ZioTypes {
  type RezoTask[A] = ZIO[Any, ServerException, A]
  type RezoDBTask[A] = ZIO[Any, RepositoryException, A]
}
