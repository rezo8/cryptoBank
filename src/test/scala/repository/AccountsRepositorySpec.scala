package repository

import cats.effect.*
import doobie.util.transactor.Transactor.Aux
import fixtures.UsersFixtures
import models.Account
import repository.AccountsRepositorySpec.test
import repository.Exceptions.{
  ForeignKeyViolationUser,
  MissingAccountByAccountId,
  MissingAccountByUserIdAndCryptoType,
  UniqueViolationUserCryptoType
}
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
    test("properly create and load account ") {
      for {
        (_, userId, accountId) <- setupUserAndAccount
        loadedAccount <- accountsRepository.getAccountByAccountId(
          accountId
        )
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
      "properly loads by cryptoType and userId]"
    ) {
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
    test("properly create and load multiple account for user") {
      for {
        (_, userId, accountId) <- setupUserAndAccount
        accountId2 <- accountsRepository.createAccount(
          userId,
          "ETH",
          "test account"
        )
        loadedAccounts <- accountsRepository.getAccountsByUserId(
          userId
        )
      } yield assertTrue(
        loadedAccounts.size == 2
          && loadedAccounts.exists(_.cryptoType == "ETH")
          && loadedAccounts.exists(_.cryptoType == "BTC")
      )
    },
    test("fails with ForeignKeyViolationUser when adding for user that DNE") {
      val randomId = UUID.randomUUID()
      assertZIO(
        accountsRepository
          .createAccount(randomId, defaultCryptoType, "test account")
          .exit
      )(fails(equalTo(ForeignKeyViolationUser(randomId))))
    },
    test(
      "fails with MissingAccountByUserIdAndCryptoType when loading account that doesn't have crypto type."
    ) {
      for {
        (_, userId, accountId) <- setupUserAndAccount
        test <- assertZIO(
          accountsRepository
            .getAccountsByUserIdAndCryptoType(
              userId,
              "DNE Crypto" // TODO random string
            )
            .exit
        )(
          fails(
            equalTo(MissingAccountByUserIdAndCryptoType(userId, "DNE Crypto"))
          )
        )
      } yield test
    },
    test(
      "fails with MissingAccountByAccountId when user does not exist"
    ) {
      val randomId = UUID.randomUUID()
      assertZIO(
        accountsRepository.getAccountByAccountId(randomId).exit
      )(fails(equalTo(MissingAccountByAccountId(randomId))))
    },
    test(
      "fails with UniqueViolationUserCryptoType when trying to create two accounts for a user with same cryptoType"
    ) {
      for {
        (_, userId, _) <- setupUserAndAccount
        test <- assertZIO(
          accountsRepository
            .createAccount(
              userId,
              defaultCryptoType,
              "test account"
            )
            .exit
        )(
          fails(
            equalTo(UniqueViolationUserCryptoType(userId, defaultCryptoType))
          )
        )
      } yield test
    }
  ) @@ TestAspect.beforeAll(initializeDb)
    @@ TestAspect.afterAll(closeDb)
}
