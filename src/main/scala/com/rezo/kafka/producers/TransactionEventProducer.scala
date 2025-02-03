package com.rezo.kafka.producers

import com.rezo.config.TransactionEventsTopicConfig
import org.apache.kafka.clients.producer.RecordMetadata
import zio.*
import zio.kafka.producer.{Producer, ProducerSettings}
import zio.kafka.serde.*

import java.time.OffsetDateTime

class TransactionEventProducer(config: TransactionEventsTopicConfig) {

  val producer: ZLayer[Any, Throwable, Producer] =
    ZLayer.scoped(Producer.make(ProducerSettings(config.bootstrapServers)))

  def produce(currTime: OffsetDateTime): RIO[Producer, RecordMetadata] = {
    Producer.produce[Any, Long, String](
      config.topicName,
      currTime.getHour,
      s"$currTime -- Hello, World!",
      keySerializer = Serde.long,
      valueSerializer = Serde.string
    )
  }
}
