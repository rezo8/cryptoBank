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
      user: User
  ): Task[Either[ServerException, UUID]] = {
    createUser(user)
      .attemptSomeSqlState {
        case sqlstate.class23.UNIQUE_VIOLATION => UserAlreadyExists()
        case _                                 => Unexpected()
      }
      .to[Task]
  }

  // Load user by ID
  def getUser(id: UUID): Task[Either[ServerException, User]] =
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
      .to[Task]

  // Load user by ID
  def getUserByEmail(email: String): Task[Either[ServerException, User]] =
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
      .to[Task]

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
