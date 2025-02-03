package com.rezo.config

import pureconfig.*
import pureconfig.generic.derivation.*

sealed trait DerivedConfig derives ConfigReader

case class EventListenerConfig(
    transactionEvents: TransactionEventsTopicConfig
) extends DerivedConfig

case class TransactionEventsTopicConfig(
    topicName: String,
    consumer: ConsumerConfig,
    //    producer: ProducerConfig,
    bootstrapServers: List[String]
)

case class ConsumerConfig(
    groupId: String,
    consumerCount: Int
)

//case class ProducerConfig()
case class ServerConfig(
    database: DatabaseConfig,
    serverMetadataConfig: ServerMetadataConfig
) extends DerivedConfig

case class DatabaseConfig(
    url: String,
    user: String,
    password: String
)

case class ServerMetadataConfig(
    port: Int
)

class ConfigLoadException extends Exception
