package repository

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import doobie.*
import doobie.implicits.*
import doobie.postgres.*
import doobie.postgres.implicits.*
import doobie.util.transactor.Transactor.Aux
import models.{UserWithCoins, WalletCoin}
import zio.*
import zio.interop.catz.*

import java.util.UUID

abstract class CoinsRepository {
  val transactor: Aux[IO, Unit]

  // TODO add method to move coin to new wallet.
  // TODO make sure that this fails on coins that add to more than 1 per wallet.
  def addCoinToWallet(
      coinId: UUID,
      coinName: String,
      walletId: UUID,
      amount: BigDecimal
  ): Task[Unit] = {
    val createQueries = for {
      _ <- createCoinSql(coinId, coinName)
      created <- addCoinToWalletSql(coinId, walletId, amount)
    } yield ()

    createQueries.transact(transactor).to[Task]
  }

  def updateCoinOwnedAmount(
      coinId: Int,
      newAmount: BigDecimal
  ): Task[RuntimeFlags] = {
    updateCoinAmount(coinId, newAmount).transact(transactor).to[Task]
  }

  def loadCoinsForWallet(walletId: UUID): Task[List[WalletCoin]] = {
    getCoinsForWalletSql(walletId).transact(transactor).to[Task]
  }

  def loadCoinsForUser(userId: UUID): Task[UserWithCoins] = {
    getCoinsForUserSql(userId)
      .transact(transactor)
      .map(walletCodes => {
        UserWithCoins(userId, walletCodes)
      })
      .to[Task]
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
  ): ConnectionIO[UUID] = {
    sql"""
         |INSERT INTO coins (coinId, coinName)
         |VALUES ($coinId, $coinName)
         |RETURNING id
         |""".stripMargin.query[UUID].unique
  }

  private def addCoinToWalletSql(
      coinId: UUID,
      walletId: UUID,
      amount: BigDecimal
  ) = {
    sql"""
         | INSERT INTO wallet_coins (coinId, walletId, amount)
         | VALUES ($coinId, $walletId, $amount)
         |""".stripMargin.update.run
  }

  private def updateCoinAmount(
      internalId: Int,
      newAmount: BigDecimal
  ) = {
    sql"""
         UPDATE wallet_coins
         SET amount = $newAmount
         WHERE id = $internalId
       """.stripMargin.update.run
  }
}
