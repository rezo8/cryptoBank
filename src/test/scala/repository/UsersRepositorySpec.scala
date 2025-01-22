package repository

import cats.effect.*
import doobie.util.transactor.Transactor.Aux
import fixtures.UsersFixtures
import repository.Exceptions.UserAlreadyExists
import zio.ZIO
import zio.test.{Spec, TestAspect, ZIOSpecDefault, assertTrue}

object UsersRepositorySpec extends ZIOSpecDefault with RepositorySpec {
  val usersRepository: UsersRepository = new UsersRepository {
    override val transactor: Aux[IO, Unit] = testTransactor
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
  ) @@ TestAspect.beforeAll(initializeDb)
    @@ TestAspect.afterAll(closeDb)
}
