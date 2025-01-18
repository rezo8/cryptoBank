package repository

import cats.effect.IO
import config.DatabaseConfig
import doobie.*
import doobie.implicits.*
import doobie.postgres.*
import doobie.postgres.implicits.*
import doobie.util.transactor.Transactor
import models.User

import java.util.UUID

abstract class UsersRepository {
  lazy val dbConfig: DatabaseConfig

  private val transactor =
    Transactor.fromDriverManager[IO](
      driver = "org.postgresql.Driver",
      url = dbConfig.url,
      user = dbConfig.user,
      password = dbConfig.password,
      logHandler = None
    )

  // Insert user
  def createUser(
      user: User
  ): IO[UUID] =
    sql"""
      INSERT INTO users (firstName, lastName, email, phoneNumber)
      VALUES (${user.firstName}, ${user.lastName}, ${user.email}, ${user.phoneNumber})
      RETURNING id
    """.query[UUID].unique.transact(transactor)

  // Load user by ID
  def getUser(id: UUID): IO[Option[User]] =
    sql"""
      SELECT id, firstName, lastName, email, phoneNumber, created_at
      FROM users
      WHERE id = $id
    """.query[User].option.transact(transactor)

  // Load user by ID
  def getUserByEmail(email: String): IO[Option[User]] =
    sql"""
      SELECT id, firstName, lastName, email, phoneNumber, created_at
      FROM users
      WHERE email = $email
    """.query[User].option.transact(transactor)

  // Update user
  def updateUser(
      id: UUID,
      firstName: String,
      lastName: String,
      email: String,
      phoneNumber: String
  ): IO[Boolean] =
    sql"""
      UPDATE users
      SET firstName = $firstName, lastName = $lastName, email = $email, phoneNumber = $phoneNumber
      WHERE id = $id
    """.update.run.transact(transactor).map(_ > 0)
}
