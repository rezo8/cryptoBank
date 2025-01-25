package repository

import cats.effect.*
import doobie.util.transactor.Transactor.Aux
import fixtures.UsersFixtures
import models.CoinValue
import org.postgresql.util.PSQLException
import repository.CoinsRepositorySpec.coinsRepository.createCoin
import repository.UsersRepositorySpec.usersRepository
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
  val walletsRepository: WalletsRepository = new WalletsRepository:
    override val transactor: Aux[IO, Unit] = testTransactor

  val user = UsersFixtures.nextUser()
  val coinId = UUID.randomUUID()
  val coinName = "Test Coin"
  val coinValue = BigDecimal(Random.nextFloat())

  var userId: UUID = UUID.randomUUID()

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
        createdWallet <- walletsRepository.safeCreateWallet(
          userId,
          "test wallet 2"
        )
        createdWalletId <- ZIO.fromEither(createdWallet)
        _ <- createCoin(coinId, coinName)
        addCoinToWalletRes <- coinsRepository.addCoinToWallet(
          coinId,
          createdWalletId,
          coinValue
        )
        loadedWalletCoin <- coinsRepository.loadWalletCoinById(
          addCoinToWalletRes
        )
      } yield assertTrue({
        coinId == loadedWalletCoin.getOrElse(throw new Exception()).coinId
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
        createdWallet <- walletsRepository.safeCreateWallet(
          userId,
          "test wallet"
        )
        createdWalletId <- ZIO.fromEither(createdWallet)
        _ <- createCoin(coinId, coinName)
        addCoinToWalletRes <- coinsRepository.addCoinToWallet(
          coinId,
          createdWalletId,
          coinValue
        )
        _ <- coinsRepository.updateCoinOwnedSatoshi(
          addCoinToWalletRes,
          newValue
        )
        loadedWalletCoin <- coinsRepository.loadWalletCoinById(
          addCoinToWalletRes
        )
      } yield assertTrue(
        loadedWalletCoin
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
        createdWallet <- walletsRepository.safeCreateWallet(
          userId,
          "test wallet"
        )
        createdWalletId <- ZIO.fromEither(createdWallet)
        _ <- createCoin(coinId, coinName)
        addCoinToWalletRes <- coinsRepository.addCoinToWallet(
          coinId,
          createdWalletId,
          coinValue
        )
        createdWallet2 <- walletsRepository.safeCreateWallet(
          userId,
          "test wallet 2"
        )
        secondWallet <- ZIO.fromEither(createdWallet)
        failureFut <- coinsRepository
          .addCoinToWallet(
            coinId,
            secondWallet,
            overflowValue
          )
          .exit
      } yield assert(failureFut)(fails(isSubtype[PSQLException](anything)))
    },
    test("should fail when wallet does not exist") {
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
        addCoinToWalletRes <- coinsRepository
          .addCoinToWallet(
            coinId,
            UUID.randomUUID(),
            coinValue
          )
          .exit
      } yield assert(addCoinToWalletRes)(
        fails(isSubtype[PSQLException](anything))
      )
    }
  ) @@ TestAspect.beforeAll(initializeDb)
    @@ TestAspect.afterAll(closeDb)
}
