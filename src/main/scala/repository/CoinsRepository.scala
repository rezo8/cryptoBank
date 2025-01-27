package repository

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import doobie.*
import doobie.implicits.*
import doobie.postgres.*
import doobie.postgres.implicits.*
import doobie.util.transactor.Transactor.Aux
import models.{CoinValue, UserWithCoins, AccountCoin}
import repository.Exceptions.{
  AccountCoinIsMissingForId,
  ServerException,
  Unexpected
}
import zio.*
import zio.interop.catz.*

import java.util.UUID

abstract class CoinsRepository {
  val transactor: Aux[IO, Unit]

  // TODO add method to move coin to new account.
  // TODO make sure that this fails on coins that add to more than 1 per account.
  def addCoinToAccount(
      coinId: UUID,
      accountId: UUID,
      coinValue: CoinValue
  ): Task[UUID] = {
    addCoinToAccountSql(
      coinId,
      accountId,
      coinValue.satoshis
    ).transact(transactor).to[Task]
  }

  // TODO remove the create coin concept. Coins exist outside and are attributed to accounts.
  def createCoin(coinId: UUID, coinName: String): Task[RuntimeFlags] = {
    createCoinSql(coinId, coinName).transact(transactor).to[Task]
  }

  def updateAccountCoinOwnedSatoshi(
      accountCoinId: UUID,
      coinValue: CoinValue
  ): Task[RuntimeFlags] = {
    updateAccountCoinSatoshi(accountCoinId, coinValue.satoshis)
      .transact(transactor)
      .to[Task]
  }

  def loadCoinsForAccount(accountId: UUID): Task[List[AccountCoin]] = {
    getCoinsForAccountSql(accountId).transact(transactor).to[Task]
  }

  def loadAccountCoinById(
      accountCoinId: UUID
  ): Task[Either[ServerException, AccountCoin]] = {
    getAccountCoinByAccountCoinIdSql(accountCoinId)
      .transact(transactor)
      .to[Task]
      .fold(
        error => { Left(Unexpected()) },
        _.fold(Left(AccountCoinIsMissingForId(accountCoinId)))(Right(_))
      )
  }

  def loadCoinsForUser(userId: UUID): Task[UserWithCoins] = {
    getCoinsForUserSql(userId)
      .transact(transactor)
      .map(accountCodes => {
        UserWithCoins(userId, accountCodes)
      })
      .to[Task]
  }

  private def getAccountCoinByAccountCoinIdSql(
      accountCoinId: UUID
  ): ConnectionIO[Option[AccountCoin]] = {
    sql"""
           SELECT accountCoinId, coinId, accountId, satoshis from accountCoins where accountCoinId = $accountCoinId
         """.stripMargin.query[AccountCoin].option
  }

  private def getCoinsForAccountSql(
      accountId: UUID
  ): ConnectionIO[List[AccountCoin]] = {
    sql"""
           SELECT * from accountCoins where accountId = $accountId
         """.stripMargin.query[AccountCoin].to[List]
  }

  private def getCoinsForUserSql(
      userId: UUID
  ): ConnectionIO[List[AccountCoin]] = {
    sql"""
        SELECT wc.*
        FROM users u
        JOIN accounts w ON u.id = w.userId
        JOIN accountCoins wc ON w.id = wc.accountId
        JOIN coins c ON wc.coinId = c.coinId
        WHERE u.id = $userId
      """.query[AccountCoin].to[List]
  }

  private def createCoinSql(
      coinId: UUID,
      coinName: String
  ): ConnectionIO[Int] = {
    sql"""
         |  INSERT INTO coins (coinId, coinName)
         |  VALUES ($coinId, $coinName)
         |""".stripMargin.update.run
  }

  private def addCoinToAccountSql(
      coinId: UUID,
      accountId: UUID,
      satoshis: Long
  ): ConnectionIO[UUID] = {
    sql"""
         | INSERT INTO accountCoins (coinId, accountId, satoshis)
         | VALUES ($coinId, $accountId, $satoshis)
         | RETURNING accountCoinId
         |""".stripMargin.query[UUID].unique
  }

  private def updateAccountCoinSatoshi(
      internalId: UUID,
      newSatoshi: Long
  ) = {
    sql"""
         UPDATE accountCoins
         SET satoshis = $newSatoshi
         WHERE accountCoinId = $internalId
       """.stripMargin.update.run
  }
}
