package services

import models.{Address, BitcoinAddressValue}
import repository.{
  AddressesRepository,
  AddressesRepositoryTrait,
  UsersRepositoryTrait
}
import utils.ZioTypes.RezoTask
import zio.*

import java.util.UUID

class AddressesService(addressRepository: AddressesRepositoryTrait)
    extends RepositoryService {

  def createBitcoinAddress(
      accountId: UUID,
      address: String,
      balance: BitcoinAddressValue
  ): RezoTask[UUID] = {
    addressRepository
      .createAddress(accountId, address, balance.satoshis)
      .mapError(handleRepositoryExceptions)
  }

  def getAddressByAddressId(
      addressId: UUID
  ): RezoTask[Address] = {
    addressRepository
      .getAddressByAddressId(addressId)
      .mapError(handleRepositoryExceptions)
  }

  def getAddressesByAccountId(accountId: UUID): RezoTask[List[Address]] = {
    addressRepository
      .getAddressesByAccountId(accountId)
      .mapError(handleRepositoryExceptions)
  }

  def updateBitcoinAddressValue(
      addressId: UUID,
      addressValue: BitcoinAddressValue
  ): RezoTask[RuntimeFlags] = {
    addressRepository
      .updateAddressValue(addressId, addressValue.satoshis)
      .mapError(handleRepositoryExceptions)
  }
}

object AddressesService {
  val live: URLayer[AddressesRepositoryTrait, AddressesService] =
    ZLayer.fromFunction(new AddressesService(_))
}
