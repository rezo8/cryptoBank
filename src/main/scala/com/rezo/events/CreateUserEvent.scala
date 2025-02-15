package com.rezo.events

import com.rezo.models.User
import zio.ZIO
import zio.json.*
import zio.kafka.serde.Serde

import java.time.Instant
import java.util.UUID

final case class CreateUserEvent(
    eventId: UUID = UUID.randomUUID(),
    eventTime: Instant = Instant.now(),
    user: User
) extends BaseEvent {
  override def eventType: String = "createUserEvent"
}

object CreateUserEvent {
  implicit val encoder: JsonEncoder[CreateUserEvent] =
    DeriveJsonEncoder.gen[CreateUserEvent]
  implicit val decoder: JsonDecoder[CreateUserEvent] =
    DeriveJsonDecoder.gen[CreateUserEvent]

  val serde: Serde[Any, CreateUserEvent] = Serde.string.inmapZIO { str =>
    ZIO
      .fromEither(str.fromJson[CreateUserEvent])
      .mapError(e =>
        new RuntimeException(s"Failed to deserialize CreateUserEvent: $e")
      )
  } { createUserEvent =>
    ZIO.succeed(createUserEvent.toJson)
  }
}
