package repository

import cats.effect.*
import doobie.util.transactor.Transactor.Aux
import fixtures.UsersFixtures
import models.Account
import repository.Exceptions.{AccountAlreadyExists, AccountIsMissingByUserUUID}
import repository.UsersRepositorySpec.suite
import zio.ZIO
import zio.test.{Spec, TestAspect, ZIOSpecDefault, assertTrue}

import java.util.UUID

object AccountsRepositorySpec extends ZIOSpecDefault with RepositorySpec {
  val usersRepository: UsersRepository = new UsersRepository:
    override val transactor: Aux[IO, Unit] = testTransactor
  val accountsRepository: AccountsRepository = new AccountsRepository:
    override val transactor: Aux[IO, Unit] = testTransactor

  def spec: Spec[Any, Throwable] = suite("AccountsRepositorySpec")(
    test("properly create and load account ") {
      val user = UsersFixtures.nextUser()
      for {
        uuidEither <- usersRepository.safeCreateUser(
          user.userTypeId,
          user.firstName,
          user.lastName,
          user.email,
          user.phoneNumber,
          user.passwordHash
        )
        userId = uuidEither.getOrElse(throw new Exception())
        createdAccount <- accountsRepository.safeCreateAccount(
          userId,
          "BTC",
          "test account"
        )
        createdAccountId <- ZIO.fromEither(createdAccount)
        loadedAccountEither <- accountsRepository.getAccountsByUserId(userId)
        loadedAccounts <- ZIO.fromEither(loadedAccountEither)
        loadedAccount = loadedAccounts.head
      } yield assertTrue(
        loadedAccount == Account(
          id = createdAccountId,
          userId = userId,
          cryptoType = "BTC",
          balance = 0,
          accountName = "test account",
          createdAt = loadedAccount.createdAt,
          updatedAt = loadedAccount.updatedAt
        )
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
        ) == AccountIsMissingByUserUUID(randomId)
      })
    },
    test(
      "fails with AccountAlreadyExists when trying to create two accounts for a user with same cryptoType"
    ) {
      val user = UsersFixtures.nextUser()
      val randomId = UUID.randomUUID()
      for {
        uuidEither <- usersRepository.safeCreateUser(
          user.userTypeId,
          user.firstName,
          user.lastName,
          user.email,
          user.phoneNumber,
          user.passwordHash
        )
        uuid = uuidEither.getOrElse(throw new Exception())
        createdAccount <- accountsRepository.safeCreateAccount(
          uuid,
          "BTC",
          "test account"
        )
        createdAccountFailure <- accountsRepository.safeCreateAccount(
          uuid,
          "BTC",
          "test account"
        )
      } yield assertTrue({
        createdAccountFailure.left.getOrElse(
          throw new Exception()
        ) == AccountAlreadyExists(uuid)
      })
    }
  ) @@ TestAspect.beforeAll(initializeDb)
    @@ TestAspect.afterAll(closeDb)
}
