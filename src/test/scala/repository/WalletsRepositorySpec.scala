package repository

import cats.effect.*
import config.{AppConfig, ConfigLoadException, DatabaseConfig, DerivedConfig}
import doobie.util.transactor.Transactor
import doobie.util.transactor.Transactor.Aux
import fixtures.UsersFixtures
import models.Wallet
import pureconfig.ConfigSource
import repository.Exceptions.{WalletAlreadyExists, WalletIsMissingByUserUUID}
import repository.UsersRepositorySpec.suite
import zio.ZIO
import zio.test.{Spec, ZIOSpecDefault, assertTrue}

import java.util.UUID
import scala.concurrent.ExecutionContext

object WalletsRepositorySpec extends ZIOSpecDefault {
  implicit val ec: ExecutionContext =
    scala.concurrent.ExecutionContext.Implicits.global

  val dbConfig: DatabaseConfig = ConfigSource.default
    .at("app")
    .load[DerivedConfig]
    .getOrElse(throw new ConfigLoadException())
    .asInstanceOf[AppConfig]
    .database

  private val testTransactor: Aux[IO, Unit] =
    Transactor.fromDriverManager[IO](
      driver = "org.postgresql.Driver",
      url = dbConfig.url,
      user = dbConfig.user,
      password = dbConfig.password,
      logHandler = None
    )

  val usersRepository: UsersRepository = new UsersRepository:
    override val transactor: Aux[IO, Unit] = testTransactor

  val walletsRepository: WalletsRepository = new WalletsRepository {
    override val transactor: Aux[IO, Unit] = testTransactor
  }

  def spec: Spec[Any, Throwable] = suite("WalletsRepositorySpec")(
    test("properly create and load wallet ") {
      val user = UsersFixtures.nextUser()
      for {
        uuidEither <- usersRepository.safeCreateUser(user)
        userId = uuidEither.getOrElse(throw new Exception())
        createdWallet <- walletsRepository.safeCreateWallet(
          userId,
          "test wallet"
        )
        createdWalletId <- ZIO.fromEither(createdWallet)
        loadedWalletEither <- walletsRepository.getWalletByUserId(userId)
        loadedWallet <- ZIO.fromEither(loadedWalletEither)
      } yield assertTrue(
        loadedWallet == Wallet(
          id = createdWalletId,
          ownerId = userId,
          walletName = "test wallet"
        )
      )
    },
    test("should fail when wallet does not exist for a user") {
      val user = UsersFixtures.nextUser()
      val res = usersRepository.safeCreateUser(user)

      for {
        uuidEither <- usersRepository.safeCreateUser(user)
        uuid = uuidEither.getOrElse(throw new Exception())
        loadedUserEither <- usersRepository.getUserByEmail(user.email)
        loadedUser <- ZIO.fromEither(loadedUserEither)
      } yield assertTrue(user.copy(id = Some(uuid)) == loadedUser)
    },
    test(
      "fails with WalletIsMissingByUserUUID when creating a user with duplicate email"
    ) {
      val randomId = UUID.randomUUID()
      for {
        missingWallet <- walletsRepository.getWalletByUserId(randomId)
      } yield assertTrue({
        missingWallet.left.getOrElse(
          throw new Exception()
        ) == WalletIsMissingByUserUUID(randomId)
      })
    },
    test(
      "not be able to create two wallets for a user"
    ) {
      val user = UsersFixtures.nextUser()
      val randomId = UUID.randomUUID()
      for {
        uuidEither <- usersRepository.safeCreateUser(user)
        uuid = uuidEither.getOrElse(throw new Exception())
        createdWallet <- walletsRepository.safeCreateWallet(uuid, "test wallet")
        loadedWallet <- walletsRepository.getWalletByUserId(uuid)
        createdWalletFailure <- walletsRepository.safeCreateWallet(
          uuid,
          "test wallet"
        )
      } yield assertTrue({
        createdWalletFailure.left.getOrElse(
          throw new Exception()
        ) == WalletAlreadyExists(uuid)
      })
    }
  )
}
