package repository

import cats.effect.*
import config.{AppConfig, ConfigLoadException, DatabaseConfig, DerivedConfig}
import doobie.util.transactor.Transactor
import doobie.util.transactor.Transactor.Aux
import fixtures.UsersFixtures
import pureconfig.ConfigSource
import repository.Exceptions.UserAlreadyExists
import zio.ZIO
import zio.test.{Spec, ZIOSpecDefault, assertTrue}

import scala.concurrent.ExecutionContext

object UsersRepositorySpec extends ZIOSpecDefault {

  implicit val ec: ExecutionContext =
    scala.concurrent.ExecutionContext.Implicits.global

  val dbConfig: DatabaseConfig = ConfigSource.default
    .at("app")
    .load[DerivedConfig]
    .getOrElse(throw new ConfigLoadException())
    .asInstanceOf[AppConfig]
    .database

  val usersRepository: UsersRepository = new UsersRepository {
    override val transactor: Aux[IO, Unit] =
      Transactor.fromDriverManager[IO](
        driver = "org.postgresql.Driver",
        url = dbConfig.url,
        user = dbConfig.user,
        password = dbConfig.password,
        logHandler = None
      )
  }

  def spec: Spec[Any, Throwable] = suite("UsersRepositorySpec")(
    test("properly create and load user by id") {
      val user = UsersFixtures.nextUser()

      val res = usersRepository.safeCreateUser(user)

      for {
        uuidEither <- usersRepository.safeCreateUser(user)
        uuid = uuidEither.getOrElse(throw new Exception())
        loadedUserEither <- usersRepository.getUser(uuid)
        loadedUser <- ZIO.fromEither(loadedUserEither)
      } yield assertTrue(user.copy(id = Some(uuid)) == loadedUser)
    },
    test("properly create and load user by email") {
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
      "fails with UserAlreadyExists when creating a user with duplicate email"
    ) {
      val user = UsersFixtures.nextUser()
      val duplicateUser =
        user.copy(phoneNumber = UsersFixtures.nextPhoneNumber())

      for {
        createOne <- usersRepository.safeCreateUser(user)
        createTwo <- usersRepository.safeCreateUser(duplicateUser)
      } yield assertTrue({
        val error = createTwo.left
        error.getOrElse(throw new Exception()) == UserAlreadyExists()
      })
    }
  )
}
