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

import java.util.UUID

abstract class WalletsRepository {
  val transactor: Aux[IO, Unit]

  def safeCreateWallet(
      userId: UUID,
      walletName: String
  ): Task[Either[ServerException, UUID]] = {
    createWallet(userId, walletName)
      .attemptSomeSqlState {
        case sqlstate.class23.UNIQUE_VIOLATION => WalletAlreadyExists(userId)
        case _                                 => Unexpected()
      }
      .to[Task]
  }

  def getWalletByUserId(userId: UUID): Task[Either[ServerException, Wallet]] = {
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
      .to[Task]
  }

  // Insert Wallet
  private def createWallet(
      userId: UUID,
      walletName: String
  ): IO[UUID] =
    sql"""
      INSERT INTO wallets (ownerId, walletName)
      VALUES ($userId, $walletName)
      RETURNING id
    """.query[UUID].unique.transact(transactor)

}
