package com.rezo.services

import com.rezo.models.Account
import com.rezo.repository.AccountsRepositoryTrait
import com.rezo.repository.Exceptions.RepositoryException
import com.rezo.utils.ZioTypes.RezoServerTask
import zio.*

import java.util.UUID

class AccountsService(accountsRepository: AccountsRepositoryTrait)
    extends RepositoryService {

  def createAccount(
      userId: UUID,
      cryptoType: String,
      accountName: String
  ): RezoServerTask[UUID] = {
    accountsRepository
      .createAccount(userId, cryptoType, accountName)
      .mapError(handleRepositoryExceptions)
  }

  def getAccountByAccountId(
      accountId: UUID
  ): RezoServerTask[Account] = {
    accountsRepository
      .getAccountByAccountId(accountId)
      .mapError(handleRepositoryExceptions)
  }

  def getAccountsByUserId(userId: UUID): RezoServerTask[List[Account]] = {
    accountsRepository
      .getAccountsByUserId(userId)
      .mapError(handleRepositoryExceptions)
  }

  def getAccountsByUserIdAndCryptoType(
      userId: UUID,
      cryptoType: String
  ): RezoServerTask[Account] = {
    accountsRepository
      .getAccountsByUserIdAndCryptoType(userId, cryptoType)
      .mapError(handleRepositoryExceptions)
  }
}
object AccountsService {
  val live: URLayer[AccountsRepositoryTrait, AccountsService] =
    ZLayer.fromFunction(new AccountsService(_))
}
