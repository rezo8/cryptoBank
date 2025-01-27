package repository

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import doobie.*
import doobie.implicits.*
import doobie.postgres.*
import doobie.postgres.implicits.*
import doobie.util.transactor.Transactor.Aux
import models.Wallet
import repository.Exceptions.*
import zio.*
import zio.interop.catz.*

import java.time.Instant
import java.util.UUID

abstract class WalletsRepository {
  val transactor: Aux[IO, Unit]

  def safeCreateWallet(
      userId: UUID,
      currency: String,
      walletName: String
  ): Task[Either[ServerException, UUID]] = {
    createWallet(userId, currency, walletName)
      .attemptSomeSqlState { case sqlstate.class23.UNIQUE_VIOLATION =>
        WalletAlreadyExists(userId)
      }
      .to[Task]
  }

  def getWalletsByUserId(
      userId: UUID
  ): Task[Either[ServerException, List[Wallet]]] = {
    sql"""
      SELECT *
      FROM wallets
      WHERE userId = $userId
    """
      .query[Wallet]
      .to[List]
      .transact(transactor)
      .map(loaded => {
        if (loaded.isEmpty) {
          Left(WalletIsMissingByUserUUID(userId))
        } else {
          Right(loaded)
        }
      })
      .to[Task]
  }

  // Insert Wallet
  private def createWallet(
      userId: UUID,
      currency: String,
      walletName: String
  ): IO[UUID] =
    sql"""
      INSERT INTO wallets (userId, currency, walletName)
      VALUES ($userId, $currency, $walletName)
      RETURNING walletId
    """.query[UUID].unique.transact(transactor)

}
