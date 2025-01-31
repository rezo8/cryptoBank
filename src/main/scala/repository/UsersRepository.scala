package repository

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import doobie.*
import doobie.implicits.*
import doobie.postgres.*
import doobie.postgres.implicits.*
import doobie.util.invariant.UnexpectedEnd
import doobie.util.transactor.Transactor.Aux
import models.User
import repository.Exceptions.*
import utils.ZioTypes.RezoDBTask
import zio.*
import zio.interop.catz.*

import java.util.UUID

trait UsersRepositoryTrait {
  def getUser(userId: UUID): RezoDBTask[User]

  def getUserByEmail(email: String): RezoDBTask[User]

  def createUser(
      userTypeId: Int,
      firstName: String,
      lastName: String,
      email: String,
      phoneNumber: String,
      passwordHash: String
  ): RezoDBTask[UUID]
}

class UsersRepository(transactor: Aux[IO, Unit]) extends UsersRepositoryTrait {

  // Load user by userId
  def getUser(userId: UUID): RezoDBTask[User] =
    sql"""
      SELECT userId, userTypeId, firstName, lastName, email, phoneNumber, passwordHash, createdAt, updatedAt
      FROM users
      WHERE userId = $userId
    """
      .query[User]
      .unique
      .transact(transactor)
      .to[Task]
      .mapError({
        case UnexpectedEnd => MissingUserById(userId)
        case e @ _         => UnexpectedError(e.getMessage)
      })

  // Load user by Email
  def getUserByEmail(email: String): RezoDBTask[User] =
    sql"""
      SELECT userId, userTypeId, firstName, lastName, email, phoneNumber, passwordHash, createdAt, updatedAt
      FROM users
      WHERE email = $email
    """
      .query[User]
      .unique
      .transact(transactor)
      .to[Task]
      .mapError({
        case UnexpectedEnd => MissingUserByEmail(email)
        case e @ _         => UnexpectedError(e.getMessage)
      })

  // Insert user
  def createUser(
      userTypeId: Int,
      firstName: String,
      lastName: String,
      email: String,
      phoneNumber: String,
      passwordHash: String
  ): RezoDBTask[UUID] =
    sql"""
      INSERT INTO users (userTypeId, firstName, lastName, email, phoneNumber, passwordHash)
      VALUES ($userTypeId, $firstName, $lastName, $email, $phoneNumber, $passwordHash)
      RETURNING userId
    """
      .query[UUID]
      .unique
      .transact(transactor)
      .attemptSomeSqlState { case sqlstate.class23.UNIQUE_VIOLATION =>
        UniqueViolationUser(email, phoneNumber)
      }
      .to[Task]
      .absolve
      .mapError({
        case r if r.isInstanceOf[RepositoryException] => // Don't like this hack. TODO figure out
          r.asInstanceOf[RepositoryException]
        case e @ _ => UnexpectedError(e.getMessage)
      })

}
