package services

import doobie.postgres.sqlstate
import models.{Account, Address, BitcoinAddressValue, User}
import Exceptions.*
import repository.{AccountsRepository, AddressRepository, UsersRepository}
import utils.ZioTypes.RezoTask
import zio.*

import java.util.UUID

class AccountsService(accountsRepository: AccountsRepository) {

  def createAccount(
      userId: UUID,
      cryptoType: String,
      accountName: String
  ): RezoTask[UUID] = {
    accountsRepository
      .createAccount(userId, cryptoType, accountName)
      .mapError {
        // Sad i think I have to move this to Doobie Code. Do I abstract away to repo exceptions?
        case sqlstate.class23.UNIQUE_VIOLATION => AccountAlreadyExists(userId)
        case e                                 => Unexpected(e)
      }
  }

  def getAccountByAccountId(
      accountId: UUID
  ): RezoTask[Account] = {
    accountsRepository
      .getAccountByAccountId(accountId)
      .mapBoth(
        error => Unexpected(error),
        _.fold(Left(AccountIsMissingByAccountId(accountId)))(Right(_))
      )
      .absolve
  }

  def getAccountsByUserId(userId: UUID): RezoTask[List[Account]] = {
    accountsRepository
      .getAccountsByUserId(userId)
      .mapError(e => Unexpected(e))
  }
}
