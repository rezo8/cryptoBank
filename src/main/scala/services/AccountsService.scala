package services

import models.Account
import repository.AccountsRepositoryTrait
import repository.Exceptions.RepositoryException
import utils.ZioTypes.RezoTask
import zio.*

import java.util.UUID

class AccountsService(accountsRepository: AccountsRepositoryTrait)
    extends RepositoryService {

  def createAccount(
      userId: UUID,
      cryptoType: String,
      accountName: String
  ): RezoTask[UUID] = {
    accountsRepository
      .createAccount(userId, cryptoType, accountName)
      .mapError(handleRepositoryExceptions)
  }

  def getAccountByAccountId(
      accountId: UUID
  ): RezoTask[Account] = {
    accountsRepository
      .getAccountByAccountId(accountId)
      .mapError(handleRepositoryExceptions)
  }

  def getAccountsByUserId(userId: UUID): RezoTask[List[Account]] = {
    accountsRepository
      .getAccountsByUserId(userId)
      .mapError(handleRepositoryExceptions)
  }

  def getAccountsByUserIdAndCryptoType(
      userId: UUID,
      cryptoType: String
  ): RezoTask[Account] = {
    accountsRepository
      .getAccountsByUserIdAndCryptoType(userId, cryptoType)
      .mapError(handleRepositoryExceptions)
  }
}
object AccountsService {
  val live: URLayer[AccountsRepositoryTrait, AccountsService] =
    ZLayer.fromFunction(new AccountsService(_))
}
