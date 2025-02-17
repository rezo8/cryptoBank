package com.rezo.kafka

import com.rezo.utils.ZioTypes.RezoException

object Exceptions {
  trait KafkaException extends RezoException

  case class PublishError(throwable: Throwable) extends KafkaException {
    override def getCause: Throwable = throwable
    override def getMessage: String = throwable.getMessage
  }
}
