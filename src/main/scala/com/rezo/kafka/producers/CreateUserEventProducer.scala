package com.rezo.kafka.producers

import com.rezo.config.TransactionEventsTopicConfig
import com.rezo.events.CreateUserEvent
import com.rezo.kafka.Exceptions.PublishError
import com.rezo.models.User
import com.rezo.utils.ZioTypes.RezoTask
import org.apache.kafka.clients.producer.RecordMetadata
import zio.*
import zio.kafka.producer.{Producer, ProducerSettings}
import zio.kafka.serde.*

import java.util.UUID

trait CreateUserEventProducerTrait {
  val producer: ZLayer[Any, Throwable, Producer]
  def produce(
      user: User
  ): RezoTask[RecordMetadata]
}

class CreateUserEventProducer(config: TransactionEventsTopicConfig)
    extends CreateUserEventProducerTrait {

  val producer: ZLayer[Any, Throwable, Producer] =
    ZLayer.scoped(Producer.make(ProducerSettings(config.bootstrapServers)))

  def produce(
      user: User
  ): RezoTask[RecordMetadata] = {
    val createUserEvent = CreateUserEvent(user = user)
    Producer
      .produce[Any, UUID, CreateUserEvent](
        config.topicName,
        user.userId,
        createUserEvent,
        keySerializer = Serde.uuid,
        valueSerializer = CreateUserEvent.serde
      )
      .mapError(exception => { PublishError(exception) })

  }
}
