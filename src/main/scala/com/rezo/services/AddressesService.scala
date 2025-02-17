package com.rezo.services

import com.rezo.models.{Address, BitcoinAddressValue}
import com.rezo.repository.{AddressesRepository, AddressesRepositoryTrait, UsersRepositoryTrait}
import com.rezo.utils.ZioTypes.RezoServerTask
import zio.*

import java.util.UUID

class AddressesService(addressRepository: AddressesRepositoryTrait)
    extends RepositoryService {

  def createBitcoinAddress(
      accountId: UUID,
      address: String,
      balance: BitcoinAddressValue
  ): RezoServerTask[UUID] = {
    addressRepository
      .createAddress(accountId, address, balance.satoshis)
      .mapError(handleRepositoryExceptions)
  }

  def getAddressByAddressId(
      addressId: UUID
  ): RezoServerTask[Address] = {
    addressRepository
      .getAddressByAddressId(addressId)
      .mapError(handleRepositoryExceptions)
  }

  def getAddressesByAccountId(accountId: UUID): RezoServerTask[List[Address]] = {
    addressRepository
      .getAddressesByAccountId(accountId)
      .mapError(handleRepositoryExceptions)
  }

  def updateBitcoinAddressValue(
      addressId: UUID,
      addressValue: BitcoinAddressValue
  ): RezoServerTask[RuntimeFlags] = {
    addressRepository
      .updateAddressValue(addressId, addressValue.satoshis)
      .mapError(handleRepositoryExceptions)
  }
}

object AddressesService {
  val live: URLayer[AddressesRepositoryTrait, AddressesService] =
    ZLayer.fromFunction(new AddressesService(_))
}
