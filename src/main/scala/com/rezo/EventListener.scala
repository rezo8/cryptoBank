package com.rezo

import com.rezo.config.{ConfigLoadException, DerivedConfig, EventListenerConfig}
import com.rezo.kafka.consumers.TransactionEventConsumer
import com.rezo.kafka.producers.CreateUserEventProducer
import pureconfig.ConfigSource
import zio.*

object EventListener extends ZIOAppDefault {
  val config: EventListenerConfig = ConfigSource.default
    .at("eventListener")
    .load[DerivedConfig]
    .getOrElse(throw new ConfigLoadException())
    .asInstanceOf[EventListenerConfig]

  private val transactionEventConsumer = new TransactionEventConsumer(
    config.transactionEvents
  )

  private val transactionEventProducer = new CreateUserEventProducer(
    config.transactionEvents
  )

  def run: ZIO[ZIOAppArgs & Scope, Throwable, Unit] =
    for {
      f <- transactionEventConsumer.run.fork
      _ <- f.join
    } yield ()

}
