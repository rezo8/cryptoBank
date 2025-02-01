package repository

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import doobie.*
import doobie.implicits.*
import doobie.postgres.*
import doobie.postgres.implicits.*
import doobie.util.invariant.UnexpectedEnd
import doobie.util.transactor.Transactor.Aux
import models.Address
import repository.Exceptions.*
import utils.ZioTypes.RezoDBTask
import zio.*
import zio.interop.catz.*

import java.time.Instant
import java.util.UUID

class AddressesRepository(transactor: Aux[IO, Unit])
    extends AddressesRepositoryTrait {

  def createAddress(
      accountId: UUID,
      address: String,
      balance: Long
  ): RezoDBTask[UUID] = {
    sql"""
        INSERT INTO addresses (accountId, address, balance)
        VALUES ($accountId, $address, $balance)
        RETURNING addressId
      """
      .query[UUID]
      .unique
      .attemptSomeSqlState {
        case sqlstate.class23.UNIQUE_VIOLATION =>
          UniqueViolationAccountIdAddress(accountId, address)
        case sqlstate.class23.FOREIGN_KEY_VIOLATION =>
          MissingAccountByAccountId(accountId)
      }
      .transact(transactor)
      .to[Task]
      .absolve
      .mapError({
        case err if err.isInstanceOf[RepositoryException] =>
          err.asInstanceOf[RepositoryException]
        case e @ _ =>
          UnexpectedError(e.getMessage)
      })
  }

  def getAddressByAddressId(
      addressId: UUID
  ): RezoDBTask[Address] = {
    sql"""
        SELECT *
        FROM addresses
        WHERE addressId = $addressId
      """
      .query[Address]
      .unique
      .transact(transactor)
      .to[Task]
      .mapError({
        case UnexpectedEnd => MissingAddressByAddressId(addressId)
        case e @ _         => UnexpectedError(e.getMessage)
      })
  }

  def getAddressesByAccountId(
      accountId: UUID
  ): RezoDBTask[List[Address]] = {
    sql"""
      SELECT *
      FROM addresses
      WHERE accountId = $accountId
    """
      .query[Address]
      .to[List]
      .transact(transactor)
      .to[Task]
      .mapError({ case e @ _ =>
        UnexpectedError(e.getMessage)
      })
  }

  def updateAddressValue(
      addressId: UUID,
      addressValue: Long
  ): RezoDBTask[RuntimeFlags] = {
    sql"""
         UPDATE addresses
         SET balance = $addressValue
         WHERE addressId = $addressId
       """.stripMargin.update.run
      .transact(transactor)
      .map(effectedRows => {
        if (effectedRows == 0) {
          Left(MissingAddressByAddressId(addressId))
        } else if (effectedRows > 1) {
          Left(ExcessiveUpdateAddresses(effectedRows))
        } else {
          Right(effectedRows)
        }
      })
      .to[Task]
      .absolve
      .mapError({
        case err if err.isInstanceOf[RepositoryException] =>
          err.asInstanceOf[RepositoryException]
        case e @ _ => UnexpectedError(e.getMessage)
      })
  }
}

trait AddressesRepositoryTrait {
  def createAddress(
      accountId: UUID,
      address: String,
      balance: Long
  ): RezoDBTask[UUID]

  def getAddressByAddressId(
      addressId: UUID
  ): RezoDBTask[Address]

  def getAddressesByAccountId(
      accountId: UUID
  ): RezoDBTask[List[Address]]

  def updateAddressValue(
      addressId: UUID,
      addressValue: Long
  ): RezoDBTask[RuntimeFlags]
}
