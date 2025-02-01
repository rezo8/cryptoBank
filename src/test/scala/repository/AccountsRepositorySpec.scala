package repository

import fixtures.UsersFixtures
import models.Account
import repository.Exceptions._
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
    userId <- usersRepository.createUser(
      user.userTypeId,
      user.firstName,
      user.lastName,
      user.email,
      user.phoneNumber,
      user.passwordHash
    )
    accountId <- accountsRepository.createAccount(
      userId,
      defaultCryptoType,
      "test account"
    )
  } yield (user, userId, accountId)

  def spec: Spec[Any, Throwable] = suite("AccountsRepositorySpec")(
    suite("createAccount")(
      test("properly creates and loads an account") {
        for {
          (_, userId, accountId) <- setupUserAndAccount
          loadedAccount <- accountsRepository.getAccountByAccountId(accountId)
        } yield assertTrue(
          loadedAccount == Account(
            accountId = accountId,
            userId = userId,
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
          (_, userId, _) <- setupUserAndAccount
          test <- assertZIO(
            accountsRepository
              .createAccount(userId, defaultCryptoType, "test account")
              .exit
          )(
            fails(
              equalTo(UniqueViolationUserCryptoType(userId, defaultCryptoType))
            )
          )
        } yield test
      }
    ),
    suite("getAccountByAccountId")(
      test("properly loads an account by accountId") {
        for {
          (_, userId, accountId) <- setupUserAndAccount
          loadedAccount <- accountsRepository.getAccountByAccountId(accountId)
        } yield assertTrue(
          loadedAccount == Account(
            accountId = accountId,
            userId = userId,
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
          (_, userId, accountId) <- setupUserAndAccount
          loadedAccount <- accountsRepository
            .getAccountsByUserIdAndCryptoType(userId, defaultCryptoType)
        } yield assertTrue(
          loadedAccount == Account(
            accountId = accountId,
            userId = userId,
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
          (_, userId, _) <- setupUserAndAccount
          test <- assertZIO(
            accountsRepository
              .getAccountsByUserIdAndCryptoType(userId, "DNE Crypto")
              .exit
          )(
            fails(
              equalTo(MissingAccountByUserIdAndCryptoType(userId, "DNE Crypto"))
            )
          )
        } yield test
      }
    ),
    suite("getAccountsByUserId")(
      test("properly loads multiple accounts for a user") {
        for {
          (_, userId, _) <- setupUserAndAccount
          accountId2 <- accountsRepository.createAccount(
            userId,
            "ETH",
            "test account"
          )
          loadedAccounts <- accountsRepository.getAccountsByUserId(userId)
        } yield assertTrue(
          loadedAccounts.size == 2 &&
            loadedAccounts.exists(_.cryptoType == "ETH") &&
            loadedAccounts.exists(_.cryptoType == "BTC")
        )
      }
    )
  ) @@ TestAspect.beforeAll(initializeDb) @@ TestAspect.afterAll(closeDb)
}
