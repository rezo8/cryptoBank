package com.rezo.repository

import com.rezo.fixtures.UsersFixtures
import com.rezo.models.Account
import com.rezo.repository.Exceptions.*
import com.rezo.repository.{AccountsRepository, UsersRepository}
import zio.ZIO
import zio.test.*
import zio.test.Assertion.*

import java.util.UUID

object AccountsRepositorySpec extends ZIOSpecDefault with RepositorySpec {
  val usersRepository: UsersRepository = new UsersRepository(testTransactor)
  val accountsRepository: AccountsRepository = new AccountsRepository(
    testTransactor
  )

  val defaultCryptoType = "BTC"

  // Shared setup for creating a user and account
  private def setupUserAndAccount = for {
    user <- ZIO.succeed(UsersFixtures.nextUser())
    dbUser <- usersRepository.createUser(
      user.userTypeId,
      user.firstName,
      user.lastName,
      user.email,
      user.phoneNumber,
      user.passwordHash
    )
    accountId <- accountsRepository.createAccount(
      dbUser.userId,
      defaultCryptoType,
      "test account"
    )
  } yield (user, accountId)

  def spec: Spec[Any, Throwable] = suite("AccountsRepositorySpec")(
    suite("createAccount")(
      test("properly creates and loads an account") {
        for {
          (user, accountId) <- setupUserAndAccount
          loadedAccount <- accountsRepository.getAccountByAccountId(accountId)
        } yield assertTrue(
          loadedAccount == Account(
            accountId = accountId,
            userId = user.userId,
            cryptoType = defaultCryptoType,
            balance = 0,
            accountName = "test account",
            createdAt = loadedAccount.createdAt,
            updatedAt = loadedAccount.updatedAt
          )
        )
      },
      test(
        "fails with ForeignKeyViolationUser when creating an account for a non-existent user"
      ) {
        val randomId = UUID.randomUUID()
        assertZIO(
          accountsRepository
            .createAccount(randomId, defaultCryptoType, "test account")
            .exit
        )(fails(equalTo(ForeignKeyViolationUser(randomId))))
      },
      test(
        "fails with UniqueViolationUserCryptoType when creating two accounts for a user with the same cryptoType"
      ) {
        for {
          (user, _) <- setupUserAndAccount
          test <- assertZIO(
            accountsRepository
              .createAccount(user.userId, defaultCryptoType, "test account")
              .exit
          )(
            fails(
              equalTo(
                UniqueViolationUserCryptoType(user.userId, defaultCryptoType)
              )
            )
          )
        } yield test
      }
    ),
    suite("getAccountByAccountId")(
      test("properly loads an account by accountId") {
        for {
          (user, accountId) <- setupUserAndAccount
          loadedAccount <- accountsRepository.getAccountByAccountId(accountId)
        } yield assertTrue(
          loadedAccount == Account(
            accountId = accountId,
            userId = user.userId,
            cryptoType = defaultCryptoType,
            balance = 0,
            accountName = "test account",
            createdAt = loadedAccount.createdAt,
            updatedAt = loadedAccount.updatedAt
          )
        )
      },
      test(
        "fails with MissingAccountByAccountId when loading a non-existent account"
      ) {
        val randomId = UUID.randomUUID()
        assertZIO(
          accountsRepository.getAccountByAccountId(randomId).exit
        )(fails(equalTo(MissingAccountByAccountId(randomId))))
      }
    ),
    suite("getAccountsByUserIdAndCryptoType")(
      test("properly loads an account by userId and cryptoType") {
        for {
          (user, accountId) <- setupUserAndAccount
          loadedAccount <- accountsRepository
            .getAccountsByUserIdAndCryptoType(user.userId, defaultCryptoType)
        } yield assertTrue(
          loadedAccount == Account(
            accountId = accountId,
            userId = user.userId,
            cryptoType = defaultCryptoType,
            balance = 0,
            accountName = "test account",
            createdAt = loadedAccount.createdAt,
            updatedAt = loadedAccount.updatedAt
          )
        )
      },
      test(
        "fails with MissingAccountByUserIdAndCryptoType when loading an account with a non-existent cryptoType"
      ) {
        for {
          (user, _) <- setupUserAndAccount
          test <- assertZIO(
            accountsRepository
              .getAccountsByUserIdAndCryptoType(user.userId, "DNE Crypto")
              .exit
          )(
            fails(
              equalTo(
                MissingAccountByUserIdAndCryptoType(user.userId, "DNE Crypto")
              )
            )
          )
        } yield test
      }
    ),
    suite("getAccountsByUserId")(
      test("properly loads multiple accounts for a user") {
        for {
          (user, _) <- setupUserAndAccount
          accountId2 <- accountsRepository.createAccount(
            user.userId,
            "ETH",
            "test account"
          )
          loadedAccounts <- accountsRepository.getAccountsByUserId(user.userId)
        } yield assertTrue(
          loadedAccounts.size == 2 &&
            loadedAccounts.exists(_.cryptoType == "ETH") &&
            loadedAccounts.exists(_.cryptoType == "BTC")
        )
      }
    )
  ) @@ TestAspect.beforeAll(initializeDb) @@ TestAspect.afterAll(closeDb)
}
