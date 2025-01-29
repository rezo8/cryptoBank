package repository

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import doobie.*
import doobie.implicits.*
import doobie.postgres.*
import doobie.postgres.implicits.*
import doobie.util.transactor.Transactor.Aux
import models.Account
import repository.Exceptions.*
import zio.*
import zio.interop.catz.*

import java.time.Instant
import java.util.UUID

abstract class AccountsRepository {
  val transactor: Aux[IO, Unit]

  def safeCreateAccount(
      userId: UUID,
      cryptoType: String,
      accountName: String
  ): Task[UUID] = {
    createAccount(userId, cryptoType, accountName).to[Task]
  }

  def getAccountByAccountId(
      accountId: UUID
  ): Task[Option[Account]] = {
    sql"""
         SELECT *
         FROM accounts
         WHERE accountId = $accountId
       """
      .query[Account]
      .option
      .transact(transactor)
      .to[Task]
  }

  def getAccountsByUserId(
      userId: UUID
  ): Task[List[Account]] = {
    sql"""
      SELECT *
      FROM accounts
      WHERE userId = $userId
    """
      .query[Account]
      .to[List]
      .transact(transactor)
      .to[Task]
  }

  // Insert Account
  private def createAccount(
      userId: UUID,
      cryptoType: String,
      accountName: String
  ): IO[UUID] =
    sql"""
      INSERT INTO accounts (userId, cryptoType, accountName)
      VALUES ($userId, $cryptoType, $accountName)
      RETURNING accountId
    """.query[UUID].unique.transact(transactor)

}
