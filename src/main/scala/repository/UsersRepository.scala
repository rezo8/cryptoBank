package repository

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import doobie.*
import doobie.implicits.*
import doobie.postgres.*
import doobie.postgres.implicits.*
import doobie.util.transactor.Transactor.Aux
import models.User
import services.Exceptions.*
import zio.*
import zio.interop.catz.*

import java.util.UUID

// Repository now fails fast with exceptions is goal.
abstract class UsersRepository {
  val transactor: Aux[IO, Unit]

  // TODO remove this
  def safeCreateUser(
      userTypeId: Int,
      firstName: String,
      lastName: String,
      email: String,
      phoneNumber: String,
      passwordHash: String
  ): Task[Either[ServerException, UUID]] = {
    createUser(
      userTypeId,
      firstName,
      lastName,
      email,
      phoneNumber,
      passwordHash
    )
      .attemptSomeSqlState { case sqlstate.class23.UNIQUE_VIOLATION =>
        UserAlreadyExists()
      }
  }

  // Load user by userId
  def getUser(userId: UUID): Task[Option[User]] =
    sql"""
      SELECT userId, userTypeId, firstName, lastName, email, phoneNumber, passwordHash, createdAt, updatedAt
      FROM users
      WHERE userId = $userId
    """
      .query[User]
      .option
      .transact(transactor)
      .to[Task]

  // Load user by Email
  def getUserByEmail(email: String): Task[Option[User]] =
    sql"""
      SELECT userId, userTypeId, firstName, lastName, email, phoneNumber, passwordHash, createdAt, updatedAt
      FROM users
      WHERE email = $email
    """
      .query[User]
      .option
      .transact(transactor)
      .to[Task]

  // Insert user
  def createUser(
      userTypeId: Int,
      firstName: String,
      lastName: String,
      email: String,
      phoneNumber: String,
      passwordHash: String
  ): Task[UUID] =
    sql"""
      INSERT INTO users (userTypeId, firstName, lastName, email, phoneNumber, passwordHash)
      VALUES ($userTypeId, $firstName, $lastName, $email, $phoneNumber, $passwordHash)
      RETURNING userId
    """.query[UUID].unique.transact(transactor).to[Task]

}
