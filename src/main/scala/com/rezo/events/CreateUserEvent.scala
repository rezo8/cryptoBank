package com.rezo.events

import com.rezo.models.User

import java.time.OffsetDateTime
import java.util.UUID

final case class CreateUserEvent(
    uuid: UUID,
    timestamp: OffsetDateTime,
    user: User,
    `type`: String = "createUserEvent"
)
