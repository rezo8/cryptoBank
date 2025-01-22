package repository

import cats.effect.IO
import config.DatabaseConfig
import doobie.*
import doobie.implicits.*
import doobie.postgres.*
import doobie.postgres.implicits.*
import doobie.util.transactor.Transactor
import doobie.util.transactor.Transactor.Aux
import models.{User, Wallet}
import repository.Exceptions.*
import zio.interop.catz.*

import java.util.UUID

abstract class WalletsRepository {
  val transactor: Aux[IO, Unit]

  def safeCreateWallet(
      userId: UUID,
      walletName: String
  ): IO[Either[ServerException, UUID]] = {
    createWallet(userId, walletName).attemptSomeSqlState {
      case sqlstate.class23.UNIQUE_VIOLATION => WalletAlreadyExists(userId)
      case _                                 => Unexpected()
    }
  }

  def getWalletByUserId(userId: UUID): IO[Either[ServerException, Wallet]] = {
    sql"""
      SELECT id, ownerId, walletName
      FROM wallets
      WHERE ownerId = ${userId}
    """
      .query[Wallet]
      .option
      .transact(transactor)
      .map(
        _.fold({
          Left(WalletIsMissingByUserUUID(userId))
        })(Right(_))
      )
  }

  // Insert Wallet
  private def createWallet(
      userId: UUID,
      walletName: String
  ): IO[UUID] =
    sql"""
      INSERT INTO wallets (ownerId, walletName)
      VALUES (${userId}, ${walletName})
      RETURNING id
    """.query[UUID].unique.transact(transactor)

}
