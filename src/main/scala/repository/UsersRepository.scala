package repository

import cats.effect.IO
import config.DatabaseConfig
import doobie.*
import doobie.implicits.*
import doobie.postgres.*
import doobie.postgres.implicits.*
import doobie.util.transactor.Transactor
import doobie.util.transactor.Transactor.Aux
import models.User
import repository.Exceptions.{
  ServerException,
  Unexpected,
  UserAlreadyExists,
  UserIsMissingByEmail,
  UserIsMissingByUUID
}
import zio.interop.catz.*

import java.util.UUID

abstract class UsersRepository {
  val transactor: Aux[IO, Unit]

  def safeCreateUser(
      user: User
  ): IO[Either[ServerException, UUID]] = {
    createUser(user).attemptSomeSqlState {
      case sqlstate.class23.UNIQUE_VIOLATION => UserAlreadyExists()
      case _                                 => Unexpected()
    }
  }

  // Load user by ID
  def getUser(id: UUID): IO[Either[ServerException, User]] =
    sql"""
      SELECT id, firstName, lastName, email, phoneNumber, created_at
      FROM users
      WHERE id = $id
    """
      .query[User]
      .option
      .transact(transactor)
      .map(
        _.fold({
          Left(UserIsMissingByUUID(id))
        })(user => {
          Right(user)
        })
      )

  // Load user by ID
  def getUserByEmail(email: String): IO[Either[ServerException, User]] =
    sql"""
      SELECT id, firstName, lastName, email, phoneNumber, created_at
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

  // Insert user
  private def createUser(
      user: User
  ): IO[UUID] =
    sql"""
      INSERT INTO users (firstName, lastName, email, phoneNumber)
      VALUES (${user.firstName}, ${user.lastName}, ${user.email}, ${user.phoneNumber})
      RETURNING id
    """.query[UUID].unique.transact(transactor)

}
