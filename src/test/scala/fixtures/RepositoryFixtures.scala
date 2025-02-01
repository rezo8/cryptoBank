package fixtures

import models.{Account, Address}

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

  def nextAccount(): Account = {
    Account(
      accountId = UUID.randomUUID(),
      userId = UUID.randomUUID(),
      cryptoType = Random.nextString(10),
      balance = Random.nextLong(10),
      accountName = Random.nextString(10),
      createdAt = Instant.now(),
      updatedAt = Instant.now()
    )
  }
}
