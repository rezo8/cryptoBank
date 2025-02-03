package messages

import java.time.OffsetDateTime
import java.util.UUID

case class UserEvent(
    uuid: UUID,
    timestamp: OffsetDateTime,
    message: Name
)
