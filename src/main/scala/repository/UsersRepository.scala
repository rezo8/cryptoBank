package repository

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import doobie.*
import doobie.implicits.*
import doobie.postgres.*
import doobie.postgres.implicits.*
import doobie.util.transactor.Transactor.Aux
import models.User
import repository.Exceptions.*
import zio.*
import zio.interop.catz.*

import java.util.UUID

// TODO add service layer to move from DBException to a Server Exception
//  rather than capturing here
abstract class UsersRepository {
  val transactor: Aux[IO, Unit]

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
      .to[Task]
  }

  // Load user by userId
  def getUser(userId: UUID): Task[Either[ServerException, User]] =
    sql"""
      SELECT userId, userTypeId, firstName, lastName, email, phoneNumber, passwordHash, createdAt, updatedAt
      FROM users
      WHERE userId = $userId
    """
      .query[User]
      .option
      .transact(transactor)
      .map(
        _.fold({
          Left(UserIsMissingByUUID(userId))
        })(user => {
          Right(user)
        })
      )
      .to[Task]

  // Load user by Email
  def getUserByEmail(email: String): Task[Either[ServerException, User]] =
    sql"""
      SELECT userId, userTypeId, firstName, lastName, email, phoneNumber, passwordHash, createdAt, updatedAt
      FROM users
      WHERE email = $email
    """
      .query[User]
      .option
      .transact(transactor)
      .map(
        _.fold({
          Left(UserIsMissingByEmail(email))
        })(user => {
          Right(user)
        })
      )
      .to[Task]

  // Insert user
  private def createUser(
      userTypeId: Int,
      firstName: String,
      lastName: String,
      email: String,
      phoneNumber: String,
      passwordHash: String
  ): IO[UUID] =
    sql"""
      INSERT INTO users (userTypeId, firstName, lastName, email, phoneNumber, passwordHash)
      VALUES ($userTypeId, $firstName, $lastName, $email, $phoneNumber, $passwordHash)
      RETURNING userId
    """.query[UUID].unique.transact(transactor)

}
