package com.rezo.kafka.consumers

// TODO organize under com.rezo

import com.rezo.config.{EventListenerConfig, TransactionEventsTopicConfig}
import org.apache.kafka.clients.consumer.ConsumerRecord
import zio.kafka.consumer.{Consumer, ConsumerSettings, Subscription}
import zio.kafka.serde.Serde
import zio.{Console, RIO, URIO, ZIO}

class TransactionEventConsumer(
    config: TransactionEventsTopicConfig
) {

  private val consumerConfig = config.consumer

  private val consumerList: Seq[
    (ConsumerRecord[Long, String] => URIO[Any, Unit]) => RIO[Any, Unit]
  ] =
    Range(0, consumerConfig.consumerCount).map(_ => {
      Consumer.consumeWith(
        settings = ConsumerSettings(config.bootstrapServers)
          .withGroupId(consumerConfig.groupId),
        subscription = Subscription.topics(config.topicName),
        keyDeserializer = Serde.long,
        valueDeserializer = Serde.string
      )
    })

  def run: RIO[Any, Unit] = {
    // TODO make sure it handles more than 1.
    consumerList.head.apply(record => processTransactionEvent(record))
  }
  private def processTransactionEvent(
      record: ConsumerRecord[Long, String]
  ): URIO[Any, Unit] = {
    Console.printLine((record.key(), record.value())).orDie
  }
}
