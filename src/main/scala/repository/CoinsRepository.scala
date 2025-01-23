package repository

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import doobie.*
import doobie.implicits.*
import doobie.postgres.*
import doobie.postgres.implicits.*
import doobie.util.transactor.Transactor.Aux
import models.{Coin, TransactionValue, UserWithCoins, WalletCoin}
import repository.Exceptions.{CoinIsMissingForId, ServerException, Unexpected}
import zio.*
import zio.interop.catz.*

import java.util.UUID

abstract class CoinsRepository {
  val transactor: Aux[IO, Unit]

  // TODO add method to move coin to new wallet.
  // TODO make sure that this fails on coins that add to more than 1 per wallet.
  def addCoinToWallet(
      coinId: UUID,
      walletId: UUID,
      amount: BigDecimal
  ): Task[Int] = {
    addCoinToWalletSql(coinId, walletId, amount).transact(transactor).to[Task]
  }

  def createCoin(coinId: UUID, coinName: String): Task[RuntimeFlags] = {
    createCoinSql(coinId, coinName).transact(transactor).to[Task]
  }

  def updateCoinOwnedAmount(
      coinId: Int,
      newAmount: BigDecimal
  ): Task[RuntimeFlags] = {
    updateWalletCoinAmount(coinId, TransactionValue(newAmount))
      .transact(transactor)
      .to[Task]
  }

  def loadCoinsForWallet(walletId: UUID): Task[List[WalletCoin]] = {
    getCoinsForWalletSql(walletId).transact(transactor).to[Task]
  }

  def loadWalletCoinById(
      coinId: Int
  ): Task[Either[ServerException, WalletCoin]] = {
    getWalletCoinByIdSql(coinId)
      .transact(transactor)
      .to[Task]
      .fold(
        error => { Left(Unexpected()) },
        _.fold(Left(CoinIsMissingForId(coinId)))(Right(_))
      )
  }

  def loadCoinsForUser(userId: UUID): Task[UserWithCoins] = {
    getCoinsForUserSql(userId)
      .transact(transactor)
      .map(walletCodes => {
        UserWithCoins(userId, walletCodes)
      })
      .to[Task]
  }

  private def getWalletCoinByIdSql(
      coinId: Int
  ): ConnectionIO[Option[WalletCoin]] = {
    sql"""
           SELECT id, coinid, walletid, amount from wallet_coins where id = $coinId
         """.stripMargin.query[WalletCoin].option
  }

  private def getCoinsForWalletSql(
      walletId: UUID
  ): ConnectionIO[List[WalletCoin]] = {
    sql"""
           SELECT * from wallet_coins where walletId = $walletId
         """.stripMargin.query[WalletCoin].to[List]
  }

  private def getCoinsForUserSql(
      userId: UUID
  ): ConnectionIO[List[WalletCoin]] = {
    sql"""
        SELECT wc.*
        FROM users u
        JOIN wallets w ON u.user_id = w.user_id
        JOIN wallet_coins wc ON w.wallet_id = wc.wallet_id
        JOIN coins c ON wc.coin_id = c.coin_id
        WHERE u.user_id = $userId
      """.query[WalletCoin].to[List]
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

  private def addCoinToWalletSql(
      coinId: UUID,
      walletId: UUID,
      amount: TransactionValue
  ): ConnectionIO[Int] = {
    sql"""
         | INSERT INTO wallet_coins (coinId, walletId, amount)
         | VALUES ($coinId, $walletId, ${amount.toSatoshis})
         | RETURNING id
         |""".stripMargin.query[Int].unique
  }

  private def updateWalletCoinAmount(
      internalId: Int,
      newAmount: TransactionValue
  ) = {
    sql"""
         UPDATE wallet_coins
         SET amount = ${newAmount.toSatoshis}
         WHERE id = $internalId
       """.stripMargin.update.run
  }
}
