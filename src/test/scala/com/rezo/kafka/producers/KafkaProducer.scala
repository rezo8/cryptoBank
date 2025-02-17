package com.rezo.kafka.producers

import zio.Task
import zio.kafka.serde.Serializer

trait KafkaProducer {
  def produce[K, V](
      topic: String,
      key: K,
      value: V,
      keySerializer: Serializer[Any, K],
      valueSerializer: Serializer[Any, V]
  ): Task[Unit]
}
