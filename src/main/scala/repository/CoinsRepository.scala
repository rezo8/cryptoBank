package repository

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import doobie.*
import doobie.implicits.*
import doobie.postgres.*
import doobie.postgres.implicits.*
import doobie.util.transactor.Transactor.Aux
import models.{CoinValue, UserWithCoins, WalletCoin}
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
      coinValue: CoinValue
  ): Task[Int] = {
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

  def updateCoinOwnedSatoshi(
      coinId: Int,
      coinValue: CoinValue
  ): Task[RuntimeFlags] = {
    updateWalletCoinSatoshi(coinId, coinValue.satoshis)
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
           SELECT id, coinid, walletid, satoshis from wallet_coins where id = $coinId
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
        JOIN wallets w ON u.id = w.ownerId
        JOIN wallet_coins wc ON w.id = wc.walletId
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
  ): ConnectionIO[Int] = {
    sql"""
         | INSERT INTO wallet_coins (coinId, walletId, satoshis)
         | VALUES ($coinId, $walletId, $satoshis)
         | RETURNING id
         |""".stripMargin.query[Int].unique
  }

  private def updateWalletCoinSatoshi(
      internalId: Int,
      newSatoshi: Long
  ) = {
    sql"""
         UPDATE wallet_coins
         SET satoshis = $newSatoshi
         WHERE id = $internalId
       """.stripMargin.update.run
  }
}
