package services

import doobie.postgres.sqlstate
import models.{Address, BitcoinAddressValue, User}
import Exceptions.*
import repository.{AddressRepository, UsersRepository}
import utils.ZioTypes.RezoTask
import zio.*

import java.util.UUID

class AddressesService(addressRepository: AddressRepository) {

  def createBitcoinAddress(
      accountId: UUID,
      address: String,
      balance: BitcoinAddressValue
  ): RezoTask[UUID] = {
    addressRepository
      .createAddressSql(accountId, address, balance.satoshis)
      .mapError(e => Unexpected(e))
  }

  def getAddressByAddressId(
      addressId: UUID
  ): RezoTask[Address] = {
    addressRepository
      .getAddressByAddressId(addressId)
      .mapBoth(
        error => Unexpected(error),
        _.fold(Left(AddressIsMissingByAddressId(addressId)))(Right(_))
      )
      .absolve
  }

  def getAddressesByAccountId(accountId: UUID): RezoTask[List[Address]] = {
    addressRepository
      .getAddressesByAccountId(accountId)
      .mapError(e => Unexpected(e))
  }

  def updateBitcoinAddressValue(
      addressId: UUID,
      addressValue: BitcoinAddressValue
  ): RezoTask[RuntimeFlags] = {
    addressRepository
      .updateBitcoinAddressValue(addressId, addressValue)
      .mapError(e => Unexpected(e))
  }
}
