package com.rezo.events

import java.time.Instant
import java.util.UUID

trait BaseEvent {
  val eventId: UUID
  val eventTime: Instant
  def eventType: String
}
