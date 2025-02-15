package com.rezo.events

import zio.ZIO
import zio.json.*
import zio.kafka.serde.Serde

import java.time.Instant
import java.util.UUID

final case class TransferCreatedEvent(
                                       eventId: UUID,
                                       eventTime: Instant,
                                       destinationAddressId: UUID,
                                       sourceAddressId: UUID,
                                       initializingUserId: UUID,
                                       amount: Long,
                                       cryptoType: String
) extends BaseEvent {
  override def eventType: String = "transferCreatedEvent"
}

object TransferCreatedEvent {
  implicit val encoder: JsonEncoder[TransferCreatedEvent] =
    DeriveJsonEncoder.gen[TransferCreatedEvent]
  implicit val decoder: JsonDecoder[TransferCreatedEvent] =
    DeriveJsonDecoder.gen[TransferCreatedEvent]

  val serde: Serde[Any, TransferCreatedEvent] = Serde.string.inmapZIO { str =>
    ZIO
      .fromEither(str.fromJson[TransferCreatedEvent])
      .mapError(e =>
        new RuntimeException(s"Failed to deserialize TransferCreatedEvent: $e")
      )
  } { user =>
    ZIO.succeed(user.toJson)
  }
}
