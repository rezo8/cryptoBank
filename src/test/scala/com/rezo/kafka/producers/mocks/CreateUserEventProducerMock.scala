package com.rezo.kafka.producers.mocks

import com.rezo.kafka.Exceptions.PublishError
import com.rezo.kafka.producers.CreateUserEventProducerTrait
import com.rezo.models.User
import com.rezo.utils.ZioTypes.{RezoException, RezoTask}
import org.apache.kafka.clients.producer.RecordMetadata
import zio.*
import zio.kafka.producer.{Producer, ProducerSettings}
import zio.mock.*

object CreateUserEventProducerMock extends Mock[CreateUserEventProducerTrait] {
  // Define the `produce` method as an effect
  object Produce extends Effect[User, RezoException, RecordMetadata]

  // Compose the mock implementation
  val compose: URLayer[Proxy, CreateUserEventProducerTrait] =
    ZLayer {
      for {
        proxy <- ZIO.service[Proxy]
      } yield new CreateUserEventProducerTrait {
        // TODO need to properly mock this.
        override val producer: ZLayer[Any, Throwable, Producer] = ZLayer.scoped(
          Producer.make(ProducerSettings(List("localhost:29092")))
        )
        override def produce(user: User): RezoTask[RecordMetadata] =
          proxy(Produce, user)
      }
    }
}
