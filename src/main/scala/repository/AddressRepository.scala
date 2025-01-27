package repository

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import doobie.*
import doobie.implicits.*
import doobie.postgres.*
import doobie.postgres.implicits.*
import doobie.util.transactor.Transactor.Aux
import models.{Account, Address, BitcoinAddressValue}
import repository.Exceptions.*
import zio.*
import zio.interop.catz.*

import java.time.Instant
import java.util.UUID

abstract class AddressRepository {
  val transactor: Aux[IO, Unit]

  // TODO handle account DNE
  def createBitcoinAddress(
      addressId: UUID,
      address: String,
      balance: BitcoinAddressValue
  ): Task[UUID] = {
    createAddressSql(addressId, address, balance.satoshis).to[Task]
  }

  def getAddressesByAccountId(
      accountId: UUID
  ): Task[Either[ServerException, List[Address]]] = {
    sql"""
      SELECT *
      FROM addresses
      WHERE accountId = $accountId
    """
      .query[Address]
      .to[List]
      .transact(transactor)
      .map(loaded => {
        if (loaded.isEmpty) {
          Left(AddressIsMissingByAccountUUID(accountId))
        } else {
          Right(loaded)
        }
      })
      .to[Task]
  }

  def updateBitcoinAddressValue(
      accountCoinId: UUID,
      addressValue: BitcoinAddressValue
  ): Task[RuntimeFlags] = {
    updateBitcoinAddressValueSql(accountCoinId, addressValue.satoshis)
      .transact(transactor)
      .to[Task]
  }

  private def updateBitcoinAddressValueSql(
      addressId: UUID,
      newSatoshi: Long
  ) = {
    sql"""
           UPDATE addresses
           SET balance = $newSatoshi
           WHERE addressId = $addressId
         """.stripMargin.update.run
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

  private def createAddressSql(
      accountId: UUID,
      address: String,
      balance: Long
  ): IO[UUID] = {
    sql"""
      INSERT INTO addresses (accountId, address, balance)
      VALUES ($accountId, $address, $balance)
      RETURNING addressId
    """.query[UUID].unique.transact(transactor)
  }
}
