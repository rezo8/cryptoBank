package repository

import cats.effect.*
import doobie.util.transactor.Transactor.Aux
import fixtures.UsersFixtures
import models.Wallet
import repository.Exceptions.{WalletAlreadyExists, WalletIsMissingByUserUUID}
import repository.UsersRepositorySpec.{suite, usersRepository}
import zio.ZIO
import zio.test.{Spec, TestAspect, ZIOSpecDefault, assertTrue}

import java.util.UUID

object WalletsRepositorySpec extends ZIOSpecDefault with RepositorySpec {
  val usersRepository: UsersRepository = new UsersRepository:
    override val transactor: Aux[IO, Unit] = testTransactor
  val walletsRepository: WalletsRepository = new WalletsRepository:
    override val transactor: Aux[IO, Unit] = testTransactor

  def spec: Spec[Any, Throwable] = suite("WalletsRepositorySpec")(
    test("properly create and load wallet ") {
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
        createdWallet <- walletsRepository.safeCreateWallet(
          userId,
          "BTC",
          "test wallet"
        )
        createdWalletId <- ZIO.fromEither(createdWallet)
        loadedWalletEither <- walletsRepository.getWalletsByUserId(userId)
        loadedWallets <- ZIO.fromEither(loadedWalletEither)
        loadedWallet = loadedWallets.head
      } yield assertTrue(
        loadedWallet == Wallet(
          id = createdWalletId,
          userId = userId,
          currency = "BTC",
          balance = BigDecimal(0),
          walletName = "test wallet",
          createdAt = loadedWallet.createdAt,
          updatedAt = loadedWallet.updatedAt
        )
      )
    },
    test(
      "fails with WalletIsMissingByUserUUID wallet does not exist"
    ) {
      val randomId = UUID.randomUUID()
      for {
        missingWallet <- walletsRepository.getWalletsByUserId(randomId)
      } yield assertTrue({
        missingWallet.left.getOrElse(
          throw new Exception()
        ) == WalletIsMissingByUserUUID(randomId)
      })
    },
    test(
      "fails with WalletAlreadyExists when trying to create two wallets for a user with same currency"
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
        createdWallet <- walletsRepository.safeCreateWallet(
          uuid,
          "BTC",
          "test wallet"
        )
        createdWalletFailure <- walletsRepository.safeCreateWallet(
          uuid,
          "BTC",
          "test wallet"
        )
      } yield assertTrue({
        createdWalletFailure.left.getOrElse(
          throw new Exception()
        ) == WalletAlreadyExists(uuid)
      })
    }
  ) @@ TestAspect.beforeAll(initializeDb)
    @@ TestAspect.afterAll(closeDb)
}
