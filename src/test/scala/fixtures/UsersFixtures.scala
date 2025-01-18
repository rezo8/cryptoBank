package fixtures

import models.User

import java.util.UUID
import scala.util.Random

object UsersFixtures {

  def nextUser(
      id: UUID = UUID.randomUUID(),
      firstName: String = new String(Random.alphanumeric.take(10).toArray),
      lastName: String = new String(Random.alphanumeric.take(10).toArray),
      email: String = nextEmail(),
      phoneNumber: String = nextPhoneNumber()
  ): User = {
    User(
      id = id,
      firstName = firstName,
      lastName = lastName,
      email = email,
      phoneNumber = phoneNumber
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
