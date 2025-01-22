package repository

import cats.effect.*
import cats.effect.testing.scalatest.AsyncIOSpec
import doobie.util.transactor.Transactor
import doobie.util.transactor.Transactor.Aux
import fixtures.UsersFixtures
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should
import repository.Exceptions.{WalletAlreadyExists, WalletIsMissingByUserUUID}

import java.util.UUID
import scala.concurrent.ExecutionContext

class WalletsRepositorySpec
    extends AsyncFlatSpec
    with AsyncIOSpec
    with should.Matchers {

  private val testTransactor: Aux[IO, Unit] =
    Transactor.fromDriverManager[IO](
      driver = "org.postgresql.Driver",
      url = testDbConfig.url,
      user = testDbConfig.user,
      password = testDbConfig.password,
      logHandler = None
    )

  val usersRepository: UsersRepository = new UsersRepository:
    override val transactor: Aux[IO, Unit] = testTransactor

  val walletsRepository: WalletsRepository = new WalletsRepository {
    override val transactor: Aux[IO, Unit] = testTransactor
  }

  implicit val ec: ExecutionContext =
    scala.concurrent.ExecutionContext.Implicits.global

  it should "properly create and load wallet" in {
    val user = UsersFixtures.nextUser()

    val x = for {
      uuidEither <- usersRepository.safeCreateUser(user)
      uuid = uuidEither.getOrElse(throw new Exception())
      createdWallet <- walletsRepository.safeCreateWallet(uuid, "test wallet")
      loadedWallet <- walletsRepository.getWalletByUserId(uuid)
    } yield (loadedWallet, uuid)

    x.map((walletEither, userId) => {
      walletEither.fold(
        x => {
          println(x.getMessage)
          throw new Exception()
        },
        wallet => {
          wallet.ownerId should be(userId)
          wallet.walletName should be("test wallet")
        }
      )
    })
  }
  it should "fail when wallet does not exist for a user" in {
    val randomId = UUID.randomUUID()
    val x = for {
      missingWallet <- walletsRepository.getWalletByUserId(randomId)
    } yield missingWallet

    x.map(
      _.fold(
        exception => exception should be(WalletIsMissingByUserUUID(randomId)),
        success => throw new Exception()
      )
    )
  }

  it should "not be able to create two wallets for a user" in {
    val user = UsersFixtures.nextUser()

    val x = for {
      uuidEither <- usersRepository.safeCreateUser(user)
      uuid = uuidEither.getOrElse(throw new Exception())
      createdWallet <- walletsRepository.safeCreateWallet(uuid, "test wallet")
      loadedWallet <- walletsRepository.getWalletByUserId(uuid)
      createdWalletFailure <- walletsRepository.safeCreateWallet(
        uuid,
        "test wallet"
      )
    } yield (createdWalletFailure, uuid)

    x.map((createdWalletFailure, userId) => {
      createdWalletFailure.fold(
        exception => exception should be(WalletAlreadyExists(userId)),
        success => throw new Exception()
      )
    })
  }
}
