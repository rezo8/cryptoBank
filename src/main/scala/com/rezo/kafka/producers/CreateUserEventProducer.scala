package com.rezo.kafka.producers

import com.rezo.config.TransactionEventsTopicConfig
import com.rezo.events.CreateUserEvent
import com.rezo.models.User
import org.apache.kafka.clients.producer.RecordMetadata
import zio.*
import zio.kafka.producer.{Producer, ProducerSettings}
import zio.kafka.serde.*

import java.util.UUID

class CreateUserEventProducer(config: TransactionEventsTopicConfig) {

  val producer: ZLayer[Any, Throwable, Producer] =
    ZLayer.scoped(Producer.make(ProducerSettings(config.bootstrapServers)))

  def produce(
      user: User
  ): RIO[Producer, RecordMetadata] = {
    val createUserEvent = CreateUserEvent(user = user)
    Producer.produce[Any, UUID, CreateUserEvent](
      config.topicName,
      user.userId,
      createUserEvent,
      keySerializer = Serde.uuid,
      valueSerializer = CreateUserEvent.serde
    )
  }
}
