package com.rezo.services

import com.rezo.models.Account
import com.rezo.repository.AccountsRepositoryTrait
import com.rezo.fixtures.RepositoryFixtures.nextAccount
import com.rezo.repository.Exceptions.*
import com.rezo.repository.mocks.AccountsRepositoryMock
import com.rezo.services.AccountsService
import com.rezo.services.Exceptions.{DatabaseConflict, MissingDatabaseObject, Unexpected}
import zio.*
import zio.mock.Expectation.*
import zio.test.*
import zio.test.Assertion.*

import java.util.UUID

object AccountServiceSpec extends ZIOSpecDefault {

  val userId: UUID = UUID.randomUUID()
  val cryptoType = "test cryptoType"
  val accountName = "test account"

  val defaultAccount: Account = nextAccount()

  def spec: Spec[Any, Throwable] = suite("AccountsServiceSpec")(
    suite("#createAccount") {
      val program = for {
        accountsService <- ZIO.service[AccountsService]
        result <- accountsService.createAccount(
          userId,
          cryptoType,
          accountName
        )
      } yield result
      Seq(
        test("returns a UUID on success") {
          val mockEnv: ULayer[AccountsRepositoryTrait] =
            AccountsRepositoryMock
              .CreateAccount(
                equalTo(
                  (
                    userId,
                    cryptoType,
                    accountName
                  )
                ),
                value(defaultAccount.accountId)
              )
              .toLayer

          assertZIO(program.provideLayer(mockEnv >>> AccountsService.live))(
            equalTo(defaultAccount.accountId)
          )
        },
        test(
          "fails with UniqueViolation response on UniqueViolationUserCryptoType"
        ) {
          val uniqueViolationAccount =
            UniqueViolationUserCryptoType(userId, cryptoType)
          val mockEnv: ULayer[AccountsRepositoryTrait] =
            AccountsRepositoryMock
              .CreateAccount(
                equalTo(
                  (
                    userId,
                    cryptoType,
                    accountName
                  )
                ),
                failure(uniqueViolationAccount)
              )
              .toLayer

          assertZIO(program.exit)(
            fails(
              equalTo(DatabaseConflict(uniqueViolationAccount.getMessage))
            )
          ).provideLayer(mockEnv >>> AccountsService.live)
        },
        test(
          "fails with DatabaseConflict response on ForeignKeyViolationUser error"
        ) {
          val foreignKeyViolationUser = ForeignKeyViolationUser(userId)
          val mockEnv: ULayer[AccountsRepositoryTrait] =
            AccountsRepositoryMock
              .CreateAccount(
                equalTo(
                  (
                    userId,
                    cryptoType,
                    accountName
                  )
                ),
                failure(foreignKeyViolationUser)
              )
              .toLayer

          assertZIO(program.exit)(
            fails(
              equalTo(DatabaseConflict(foreignKeyViolationUser.getMessage))
            )
          ).provideLayer(mockEnv >>> AccountsService.live)
        },
        test("fails with Unexpected response on unexpected error") {
          val randomException = UnexpectedError("test")

          val mockEnv: ULayer[AccountsRepositoryTrait] =
            AccountsRepositoryMock
              .CreateAccount(
                equalTo(
                  (
                    userId,
                    cryptoType,
                    accountName
                  )
                ),
                failure(randomException)
              )
              .toLayer

          assertZIO(program.exit)(fails(equalTo(Unexpected(randomException))))
            .provideLayer(mockEnv >>> AccountsService.live)
        }
      )
    },
    suite("getAccountByAccountId") {
      val program = for {
        accountsService <- ZIO.service[AccountsService]
        result <- accountsService.getAccountByAccountId(
          defaultAccount.accountId
        )
      } yield result
      Seq(
        test("returns an Account on success") {
          val mockEnv: ULayer[AccountsRepositoryTrait] =
            AccountsRepositoryMock
              .GetAccountByAccountId(
                equalTo(defaultAccount.accountId),
                value(defaultAccount)
              )
              .toLayer

          assertZIO(program.provideLayer(mockEnv >>> AccountsService.live))(
            equalTo(defaultAccount)
          )
        },
        test("fails with MissingDatabaseObject on MissingUserByEmail error") {
          val missingAccountByAccountId =
            MissingAccountByAccountId(defaultAccount.accountId)
          val mockEnv: ULayer[AccountsRepositoryTrait] =
            AccountsRepositoryMock
              .GetAccountByAccountId(
                equalTo(defaultAccount.accountId),
                failure(missingAccountByAccountId)
              )
              .toLayer

          assertZIO(program.exit)(
            fails(
              equalTo(
                MissingDatabaseObject(missingAccountByAccountId.getMessage)
              )
            )
          ).provideLayer(mockEnv >>> AccountsService.live)
        },
        test("fails with Unexpected response on unexpected error") {
          val randomException = UnexpectedError("test")
          val mockEnv: ULayer[AccountsRepositoryTrait] =
            AccountsRepositoryMock
              .GetAccountByAccountId(
                equalTo(defaultAccount.accountId),
                failure(randomException)
              )
              .toLayer

          assertZIO(program.exit)(fails(equalTo(Unexpected(randomException))))
            .provideLayer(mockEnv >>> AccountsService.live)
        }
      )
    },
    suite("getAccountsByAccountId") {
      val program = for {
        accountsService <- ZIO.service[AccountsService]
        result <- accountsService.getAccountsByUserId(
          defaultAccount.userId
        )
      } yield result
      Seq(
        test("returns an Account on success") {
          val expectedList = List(defaultAccount, nextAccount())
          val mockEnv: ULayer[AccountsRepositoryTrait] =
            AccountsRepositoryMock
              .GetAccountsByUserId(
                equalTo(defaultAccount.userId),
                value(expectedList)
              )
              .toLayer

          assertZIO(program.provideLayer(mockEnv >>> AccountsService.live))(
            equalTo(expectedList)
          )
        },
        test("fails with Unexpected response on unexpected error") {
          val randomException = UnexpectedError("test")
          val mockEnv: ULayer[AccountsRepositoryTrait] =
            AccountsRepositoryMock
              .GetAccountsByUserId(
                equalTo(defaultAccount.userId),
                failure(randomException)
              )
              .toLayer

          assertZIO(program.exit)(fails(equalTo(Unexpected(randomException))))
            .provideLayer(mockEnv >>> AccountsService.live)
        }
      )
    },
    suite("getAccountsByUserIdAndCryptoType") {
      val program = for {
        accountsService <- ZIO.service[AccountsService]
        result <- accountsService.getAccountsByUserIdAndCryptoType(
          defaultAccount.userId,
          defaultAccount.cryptoType
        )
      } yield result
      Seq(
        test("returns an Account on success") {
          val mockEnv: ULayer[AccountsRepositoryTrait] =
            AccountsRepositoryMock
              .GetAccountsByUserIdAndCryptoType(
                equalTo(defaultAccount.userId, defaultAccount.cryptoType),
                value(defaultAccount)
              )
              .toLayer

          assertZIO(program.provideLayer(mockEnv >>> AccountsService.live))(
            equalTo(defaultAccount)
          )
        },
        test("fails with MissingDatabaseObject on MissingUserByEmail error") {
          val missingAccountByUserIdAndCryptoType =
            MissingAccountByUserIdAndCryptoType(
              defaultAccount.userId,
              defaultAccount.cryptoType
            )
          val mockEnv: ULayer[AccountsRepositoryTrait] =
            AccountsRepositoryMock
              .GetAccountsByUserIdAndCryptoType(
                equalTo(defaultAccount.userId, defaultAccount.cryptoType),
                failure(missingAccountByUserIdAndCryptoType)
              )
              .toLayer

          assertZIO(program.exit)(
            fails(
              equalTo(
                MissingDatabaseObject(
                  missingAccountByUserIdAndCryptoType.getMessage
                )
              )
            )
          ).provideLayer(mockEnv >>> AccountsService.live)
        },
        test("fails with Unexpected response on unexpected error") {
          val randomException = UnexpectedError("test")
          val mockEnv: ULayer[AccountsRepositoryTrait] =
            AccountsRepositoryMock
              .GetAccountsByUserIdAndCryptoType(
                equalTo(defaultAccount.userId, defaultAccount.cryptoType),
                failure(randomException)
              )
              .toLayer

          assertZIO(program.exit)(fails(equalTo(Unexpected(randomException))))
            .provideLayer(mockEnv >>> AccountsService.live)
        }
      )
    }
  )
}
