package repository

import cats.effect.*
import doobie.util.transactor.Transactor.Aux
import fixtures.UsersFixtures
import models.CoinValue
import org.postgresql.util.PSQLException
import repository.CoinsRepositorySpec.coinsRepository.createCoin
import zio.ZIO
import zio.test.*
import zio.test.Assertion.*

import java.util.UUID
import scala.util.Random

object CoinsRepositorySpec extends ZIOSpecDefault with RepositorySpec {
  val coinsRepository: CoinsRepository = new CoinsRepository:
    override val transactor: Aux[IO, Unit] = testTransactor
  val usersRepository: UsersRepository = new UsersRepository:
    override val transactor: Aux[IO, Unit] = testTransactor
  val accountsRepository: AccountsRepository = new AccountsRepository:
    override val transactor: Aux[IO, Unit] = testTransactor

  def spec: Spec[Any, Throwable] = suite("CoinsRepositorySpec")(
    test("properly create and load coin ") {
      val user = UsersFixtures.nextUser()
      val coinId = UUID.randomUUID()
      val coinName = "Test Coin"
      val coinValue = CoinValue.apply(Random.between(0L, 10_000_000L))
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
        _ <- createCoin(coinId, coinName)
        addCoinToAccountRes <- coinsRepository.addCoinToAccount(
          coinId,
          createdAccountId,
          coinValue
        )
        loadedAccountCoin <- coinsRepository.loadAccountCoinById(
          addCoinToAccountRes
        )
      } yield assertTrue({
        coinId == loadedAccountCoin.getOrElse(throw new Exception()).coinId
      })
    },
    test("can update coin owned amount") {
      val user = UsersFixtures.nextUser()
      val coinId = UUID.randomUUID()
      val coinName = "Test Coin"
      val coinValue = CoinValue.apply(Random.between(0L, 10_000_000L))
      val newValue = CoinValue(coinValue.satoshis - 1L)
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
        _ <- createCoin(coinId, coinName)
        addCoinToAccountRes <- coinsRepository.addCoinToAccount(
          coinId,
          createdAccountId,
          coinValue
        )
        _ <- coinsRepository.updateAccountCoinOwnedSatoshi(
          addCoinToAccountRes,
          newValue
        )
        loadedAccountCoin <- coinsRepository.loadAccountCoinById(
          addCoinToAccountRes
        )
      } yield assertTrue(
        loadedAccountCoin
          .getOrElse(throw Exception())
          .satoshis == newValue.satoshis
      )
    },
    test(
      "should fail with PSQL exception when adding more than 1 $ worth to a coin in the DB"
    ) {
      val user = UsersFixtures.nextUser()
      val coinId = UUID.randomUUID()
      val coinName = "Test Coin"
      val coinValue = CoinValue.apply(Random.between(0L, 100_000_000L))
      val overflowValue = CoinValue(100_000_000L - coinValue.satoshis + 2)
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
        _ <- createCoin(coinId, coinName)
        addCoinToAccountRes <- coinsRepository.addCoinToAccount(
          coinId,
          createdAccountId,
          coinValue
        )
        createdAccount2 <- accountsRepository.safeCreateAccount(
          userId,
          "BTC",
          "test account 2"
        )
        secondAccount <- ZIO.fromEither(createdAccount)
        failureFut <- coinsRepository
          .addCoinToAccount(
            coinId,
            secondAccount,
            overflowValue
          )
          .exit
      } yield assert(failureFut)(fails(isSubtype[PSQLException](anything)))
    },
    test("should fail when account does not exist") {
      val user = UsersFixtures.nextUser()
      val coinId = UUID.randomUUID()
      val coinName = "Test Coin"
      val coinValue = CoinValue.apply(Random.between(0L, 10_000_000L))

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
        addCoinToAccountRes <- coinsRepository
          .addCoinToAccount(
            coinId,
            UUID.randomUUID(),
            coinValue
          )
          .exit
      } yield assert(addCoinToAccountRes)(
        fails(isSubtype[PSQLException](anything))
      )
    }
  ) @@ TestAspect.beforeAll(initializeDb)
    @@ TestAspect.afterAll(closeDb)
}
