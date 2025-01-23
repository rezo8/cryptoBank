package repository

import cats.effect.*
import doobie.util.transactor.Transactor.Aux
import fixtures.UsersFixtures
import org.postgresql.util.PSQLException
import repository.CoinsRepositorySpec.coinsRepository.createCoin
import zio.ZIO
import zio.test.Assertion.*
import zio.test.*

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
      val coinValue = BigDecimal(Random.nextFloat())
      for {
        uuidEither <- usersRepository.safeCreateUser(user)
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
    test(
      "should fail with PSQL exception when adding more than 1 $ worth to a coin in the DB"
    ) {
      val user = UsersFixtures.nextUser()
      val coinId = UUID.randomUUID()
      val coinName = "Test Coin"
      val coinValue = BigDecimal(Random.nextFloat())
      val overflowValue = BigDecimal(100).+(coinValue)
      for {
        uuidEither <- usersRepository.safeCreateUser(user)
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
      val res = usersRepository.safeCreateUser(user)
      val coinId = UUID.randomUUID()
      val coinName = "Test Coin"
      val coinValue = BigDecimal(Random.nextFloat())

      for {
        uuidEither <- usersRepository.safeCreateUser(user)
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
