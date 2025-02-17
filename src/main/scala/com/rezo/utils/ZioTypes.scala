package com.rezo.utils

import com.rezo.repository.Exceptions.RepositoryException
import com.rezo.services.Exceptions.ServerException
import zio.ZIO
import zio.kafka.producer.Producer

object ZioTypes {

  trait RezoException extends Exception
  type RezoTask[A] = ZIO[Producer, RezoException, A]
  type RezoServerTask[A] = ZIO[Any, ServerException, A]
  type RezoDBTask[A] = ZIO[Any, RepositoryException, A]
}
