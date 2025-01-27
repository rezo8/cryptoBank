package repository

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import doobie.*
import doobie.implicits.*
import doobie.postgres.*
import doobie.postgres.implicits.*
import doobie.util.transactor.Transactor.Aux
import models.{CoinValue, UserWithCoins, WalletCoin}
import repository.Exceptions.{
  WalletCoinIsMissingForId,
  ServerException,
  Unexpected
}
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
      coinValue: CoinValue
  ): Task[UUID] = {
    addCoinToWalletSql(
      coinId,
      walletId,
      coinValue.satoshis
    ).transact(transactor).to[Task]
  }

  // TODO remove the create coin concept. Coins exist outside and are attributed to wallets.
  def createCoin(coinId: UUID, coinName: String): Task[RuntimeFlags] = {
    createCoinSql(coinId, coinName).transact(transactor).to[Task]
  }

  def updateWalletCoinOwnedSatoshi(
      walletCoinId: UUID,
      coinValue: CoinValue
  ): Task[RuntimeFlags] = {
    updateWalletCoinSatoshi(walletCoinId, coinValue.satoshis)
      .transact(transactor)
      .to[Task]
  }

  def loadCoinsForWallet(walletId: UUID): Task[List[WalletCoin]] = {
    getCoinsForWalletSql(walletId).transact(transactor).to[Task]
  }

  def loadWalletCoinById(
      walletCoinId: UUID
  ): Task[Either[ServerException, WalletCoin]] = {
    getWalletCoinByWalletCoinIdSql(walletCoinId)
      .transact(transactor)
      .to[Task]
      .fold(
        error => { Left(Unexpected()) },
        _.fold(Left(WalletCoinIsMissingForId(walletCoinId)))(Right(_))
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

  private def getWalletCoinByWalletCoinIdSql(
      walletCoinId: UUID
  ): ConnectionIO[Option[WalletCoin]] = {
    sql"""
           SELECT walletCoinId, coinId, walletId, satoshis from walletCoins where walletCoinId = $walletCoinId
         """.stripMargin.query[WalletCoin].option
  }

  private def getCoinsForWalletSql(
      walletId: UUID
  ): ConnectionIO[List[WalletCoin]] = {
    sql"""
           SELECT * from walletCoins where walletId = $walletId
         """.stripMargin.query[WalletCoin].to[List]
  }

  private def getCoinsForUserSql(
      userId: UUID
  ): ConnectionIO[List[WalletCoin]] = {
    sql"""
        SELECT wc.*
        FROM users u
        JOIN wallets w ON u.id = w.userId
        JOIN walletCoins wc ON w.id = wc.walletId
        JOIN coins c ON wc.coinId = c.coinId
        WHERE u.id = $userId
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
      satoshis: Long
  ): ConnectionIO[UUID] = {
    sql"""
         | INSERT INTO walletCoins (coinId, walletId, satoshis)
         | VALUES ($coinId, $walletId, $satoshis)
         | RETURNING walletCoinId
         |""".stripMargin.query[UUID].unique
  }

  private def updateWalletCoinSatoshi(
      internalId: UUID,
      newSatoshi: Long
  ) = {
    sql"""
         UPDATE walletCoins
         SET satoshis = $newSatoshi
         WHERE walletCoinId = $internalId
       """.stripMargin.update.run
  }
}
