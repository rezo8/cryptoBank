package fixtures

import models.Address

import java.time.Instant
import java.util.UUID
import scala.util.Random

object RepositoryFixtures {

  def nextAddress(): Address = {
    Address(
      addressId = UUID.randomUUID(),
      accountId = UUID.randomUUID(),
      address = "test address",
      balance = Random.nextLong(),
      isActive = true,
      createdAt = Instant.now(),
      updatedAt = Instant.now()
    )
  }
}
