package repository

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import doobie.*
import doobie.implicits.*
import doobie.postgres.*
import doobie.postgres.implicits.*
import doobie.util.invariant.UnexpectedEnd
import doobie.util.transactor.Transactor.Aux
import models.Account
import repository.Exceptions.*
import utils.ZioTypes.RezoDBTask
import zio.*
import zio.interop.catz.*

import java.time.Instant
import java.util.UUID

class AccountsRepository(transactor: Aux[IO, Unit]) {

  def createAccount(
      userId: UUID,
      cryptoType: String,
      accountName: String
  ): RezoDBTask[UUID] = {
    sql"""
      INSERT INTO accounts (userId, cryptoType, accountName)
      VALUES ($userId, $cryptoType, $accountName)
      RETURNING accountId
    """
      .query[UUID]
      .unique
      .attemptSomeSqlState {
        case sqlstate.class23.UNIQUE_VIOLATION =>
          UniqueViolationUserCryptoType(userId, cryptoType)
        case sqlstate.class23.FOREIGN_KEY_VIOLATION =>
          ForeignKeyViolationUser(userId)
      }
      .transact(transactor)
      .to[Task]
      .absolve
      .mapError({
        case err if err.isInstanceOf[RepositoryException] =>
          err.asInstanceOf[RepositoryException]
        case e @ _ => UnexpectedError(e.getMessage)
      })
  }

  def getAccountByAccountId(
      accountId: UUID
  ): RezoDBTask[Account] = {
    sql"""
         SELECT *
         FROM accounts
         WHERE accountId = $accountId
       """
      .query[Account]
      .unique
      .transact(transactor)
      .to[Task]
      .mapError({
        case UnexpectedEnd => MissingAccountByAccountId(accountId)
        case e @ _         => UnexpectedError(e.getMessage)
      })

  }

  def getAccountsByUserId(
      userId: UUID
  ): RezoDBTask[List[Account]] = {
    sql"""
      SELECT *
      FROM accounts
      WHERE userId = $userId
    """
      .query[Account]
      .to[List]
      .transact(transactor)
      .to[Task]
      .mapError({ case e @ _ =>
        UnexpectedError(e.getMessage)
      })
  }

  def getAccountsByUserIdAndCryptoType(
      userId: UUID,
      cryptoType: String
  ): RezoDBTask[Account] = {
    sql"""
       SELECT *
       FROM accounts
       WHERE userId = $userId AND cryptoType = $cryptoType
     """
      .query[Account]
      .unique
      .transact(transactor)
      .to[Task]
      .mapError({
        case UnexpectedEnd =>
          MissingAccountByUserIdAndCryptoType(userId, cryptoType)
        case e @ _ => UnexpectedError(e.getMessage)
      })
  }
}
