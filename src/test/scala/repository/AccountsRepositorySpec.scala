package repository

import cats.effect.*
import doobie.util.transactor.Transactor.Aux
import fixtures.UsersFixtures
import models.Account
import repository.Exceptions.{AccountAlreadyExists, AccountIsMissingByUserId}
import repository.UsersRepositorySpec.suite
import zio.ZIO
import zio.test.{Spec, TestAspect, ZIOSpecDefault, assertTrue}

import java.util.UUID

object AccountsRepositorySpec extends ZIOSpecDefault with RepositorySpec {
  val usersRepository: UsersRepository = new UsersRepository:
    override val transactor: Aux[IO, Unit] = testTransactor
  val accountsRepository: AccountsRepository = new AccountsRepository:
    override val transactor: Aux[IO, Unit] = testTransactor

  private def setupUserAndAccount = for {
    user <- ZIO.succeed(UsersFixtures.nextUser())
    uuidEither <- usersRepository.safeCreateUser(
      user.userTypeId,
      user.firstName,
      user.lastName,
      user.email,
      user.phoneNumber,
      user.passwordHash
    )
    userId <- ZIO.fromEither(uuidEither)
    createdAccount <- accountsRepository.safeCreateAccount(
      userId,
      "BTC",
      "test account"
    )
    accountId <- ZIO.fromEither(createdAccount)
  } yield (user, userId, accountId)

  def spec: Spec[Any, Throwable] = suite("AccountsRepositorySpec")(
    test("properly create and load account ") {
      for {
        (_, userId, accountId) <- setupUserAndAccount
        loadedAccountEither <- accountsRepository.getAccountByAccountId(
          accountId
        )
        loadedAccount <- ZIO.fromEither(loadedAccountEither)
      } yield assertTrue(
        loadedAccount == Account(
          addressId = accountId,
          userId = userId,
          cryptoType = "BTC",
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
        createdAccount2 <- accountsRepository.safeCreateAccount(
          userId,
          "ETH",
          "test account"
        )
        accountId2 <- ZIO.fromEither(createdAccount2)
        loadedAccountsEither <- accountsRepository.getAccountsByUserId(
          userId
        )
        loadedAccounts <- ZIO.fromEither(loadedAccountsEither)
      } yield assertTrue(
        loadedAccounts.size == 2
          && loadedAccounts.exists(_.cryptoType == "ETH")
          && loadedAccounts.exists(_.cryptoType == "BTC")
      )
    },
    test(
      "fails with AccountIsMissingByUserUUID account does not exist"
    ) {
      val randomId = UUID.randomUUID()
      for {
        missingAccount <- accountsRepository.getAccountsByUserId(randomId)
      } yield assertTrue({
        missingAccount.left.getOrElse(
          throw new Exception()
        ) == AccountIsMissingByUserId(randomId)
      })
    },
    test(
      "fails with AccountAlreadyExists when trying to create two accounts for a user with same cryptoType"
    ) {
      for {
        (_, userId, _) <- setupUserAndAccount
        createdAccountFailure <- accountsRepository.safeCreateAccount(
          userId,
          "BTC",
          "test account"
        )
      } yield assertTrue({
        createdAccountFailure.left.getOrElse(
          throw new Exception()
        ) == AccountAlreadyExists(userId)
      })
    }
  ) @@ TestAspect.beforeAll(initializeDb)
    @@ TestAspect.afterAll(closeDb)
}
