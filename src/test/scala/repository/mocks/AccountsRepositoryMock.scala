package repository.mocks

import models.{Account, Address}
import repository.{AccountsRepositoryTrait, AddressesRepositoryTrait}
import repository.Exceptions.RepositoryException
import utils.ZioTypes.RezoDBTask
import zio.mock.*
import zio.{URLayer, ZLayer}

import java.util.UUID

object AccountsRepositoryMock extends Mock[AccountsRepositoryTrait] {
  object GetAccountsByUserIdAndCryptoType
      extends Effect[(UUID, String), RepositoryException, Account]
  object GetAccountsByUserId
      extends Effect[UUID, RepositoryException, List[Account]]

  object GetAccountByAccountId
      extends Effect[UUID, RepositoryException, Account]

  object CreateAccount
      extends Effect[
        (UUID, String, String),
        RepositoryException,
        UUID
      ]

  object UpdateAddressValue
      extends Effect[(UUID, Long), RepositoryException, Int]

  val compose: URLayer[Proxy, AccountsRepositoryTrait] =
    ZLayer.fromFunction { (proxy: Proxy) =>
      new AccountsRepositoryTrait {
        override def createAccount(
            userId: UUID,
            cryptoType: String,
            accountName: String
        ): RezoDBTask[UUID] = proxy(
          CreateAccount,
          (userId, cryptoType, accountName)
        )

        override def getAccountByAccountId(
            accountId: UUID
        ): RezoDBTask[Account] = proxy(
          GetAccountByAccountId,
          accountId
        )

        override def getAccountsByUserId(
            userId: UUID
        ): RezoDBTask[List[Account]] = proxy(
          GetAccountsByUserId,
          userId
        )

        override def getAccountsByUserIdAndCryptoType(
            userId: UUID,
            cryptoType: String
        ): RezoDBTask[Account] = proxy(
          GetAccountsByUserIdAndCryptoType,
          (userId, cryptoType)
        )
      }
    }
}
