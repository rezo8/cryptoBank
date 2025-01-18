package repository

import cats.effect.*
import cats.effect.kernel.MonadCancelThrow
import cats.effect.testing.scalatest.AsyncIOSpec
import config.{AppConfig, ConfigLoadException, DatabaseConfig, DerivedConfig}
import fixtures.UsersFixtures
import org.postgresql.util.PSQLException
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should
import pureconfig.{ConfigSource, loadConfig}

import scala.concurrent.{ExecutionContext, Future}

class UsersRepositorySpec
    extends AsyncFlatSpec
    with AsyncIOSpec
    with should.Matchers {
  val databaseConfig: DatabaseConfig = ConfigSource.default
    .at("app")
    .load[DerivedConfig]
    .getOrElse(throw new ConfigLoadException())
    .asInstanceOf[AppConfig]
    .database

  val usersRepository = new UsersRepository {
    override lazy val dbConfig: DatabaseConfig = databaseConfig
  }

  implicit val ec: ExecutionContext =
    scala.concurrent.ExecutionContext.Implicits.global

  it should "properly create and load user" in {
    val user = UsersFixtures.nextUser()

    val res = usersRepository.createUser(user)

    val loaded = for {
      uuid <- usersRepository.createUser(user)
      loadedUser <- usersRepository.getUser(uuid)

    } yield loadedUser

    loaded.map(userOpt => {
      userOpt.isEmpty should be(false) // TODO fix this
      val loadedUser = userOpt.get
      loadedUser.firstName should be(user.firstName)
      loadedUser.lastName should be(user.lastName)
      loadedUser.email should be(user.email)
      loadedUser.phoneNumber should be(user.phoneNumber)
    })
  }

  it should "not be able to create a user with duplicate email" in {
    val user = UsersFixtures.nextUser()

    val res = usersRepository.createUser(user)

    val duplicateUser = user.copy(phoneNumber = UsersFixtures.nextPhoneNumber())

    // TODO fix the test so it can catch throwing. Maybe make return Option?
    val loaded = for {
      createOne <- usersRepository.createUser(user)
      createTwo <- usersRepository.createUser(duplicateUser)
      _ = println("test")
    } yield createTwo
    assertThrows[PSQLException](loaded)
  }

  it should "properly create and load user by email" in {
    val user = UsersFixtures.nextUser()

    val res = usersRepository.createUser(user)

    val loaded = for {
      uuid <- usersRepository.createUser(user)
      loadedUser <- usersRepository.getUserByEmail(user.email)

    } yield loadedUser

    loaded.map(userOpt => {
      userOpt.isEmpty should be(false) // TODO fix this
      val loadedUser = userOpt.get
      loadedUser.firstName should be(user.firstName)
      loadedUser.lastName should be(user.lastName)
      loadedUser.email should be(user.email)
      loadedUser.phoneNumber should be(user.phoneNumber)
    })
  }

}
