package fixtures

import models.{User, UserType}

import java.time.Instant
import java.util.UUID
import scala.util.Random

object UsersFixtures {

  def nextUserType(): UserType = {
    UserType.IdToUserTypeMap.getOrElse(
      Random.nextInt(3) + 1,
      throw new Exception()
    )
  }

  def nextUser(
      id: Option[UUID] = Some(UUID.randomUUID()),
      userTypeId: Int = nextUserType().userTypeId,
      firstName: String = new String(Random.alphanumeric.take(10).toArray),
      lastName: String = new String(Random.alphanumeric.take(10).toArray),
      email: String = nextEmail(),
      phoneNumber: String = nextPhoneNumber(),
      passwordHash: String = "test",
      createdAt: Instant = Instant.now(),
      updatedAt: Instant = Instant.now()
  ): User = {
    User(
      userId = id,
      userTypeId = userTypeId,
      firstName = firstName,
      lastName = lastName,
      email = email,
      phoneNumber = phoneNumber,
      passwordHash = passwordHash,
      createdAt = createdAt,
      updatedAt = updatedAt
    )
  }

  def nextEmail(): String = {
    new String(Random.alphanumeric.take(10).toArray) ++ "@gmail.com"
  }

  def nextPhoneNumber(): String = {
    val random = new Random()
    val digits = "0123456789".split("")
    var result = ""
    for (_ <- 0 until 10) {
      val randomIndex = random.nextInt(digits.length)
      result += digits(randomIndex)
    }
    result
  }
}
