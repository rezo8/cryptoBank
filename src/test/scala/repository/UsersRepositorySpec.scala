package repository

import cats.effect.*
import doobie.util.transactor.Transactor.Aux
import fixtures.UsersFixtures
import services.Exceptions.UserAlreadyExists
import zio.ZIO
import zio.test.{Spec, TestAspect, ZIOSpecDefault, assertTrue}

object UsersRepositorySpec extends ZIOSpecDefault with RepositorySpec {
  val usersRepository: UsersRepository = new UsersRepository {
    override val transactor: Aux[IO, Unit] = testTransactor
  }

  private def setupUser = for {
    user <- ZIO.succeed(UsersFixtures.nextUser())
    uuidEither <- usersRepository.safeCreateUser(
      user.userTypeId,
      user.firstName,
      user.lastName,
      user.email,
      user.phoneNumber,
      user.passwordHash
    )
    userId <- ZIO.fromEither(uuidEither)
  } yield (user, userId)

  def spec: Spec[Any, Throwable] = suite("UsersRepositorySpec")(
    test("properly create and load user by id") {
      for {
        (user, userId) <- setupUser
        loadedUserEither <- usersRepository.getUser(userId)
        loadedUser <- ZIO.fromEither(loadedUserEither)
      } yield assertTrue(
        user.copy(
          userId = Some(userId),
          createdAt = loadedUser.createdAt,
          updatedAt = loadedUser.updatedAt
        ) == loadedUser
      )
    },
    test("properly create and load user by email") {
      for {
        (user, userId) <- setupUser
        loadedUserEither <- usersRepository.getUserByEmail(user.email)
        loadedUser <- ZIO.fromEither(loadedUserEither)
      } yield assertTrue(
        user.copy(
          userId = Some(userId),
          createdAt = loadedUser.createdAt,
          updatedAt = loadedUser.updatedAt
        ) == loadedUser
      )
    },
    test(
      "fails with UserAlreadyExists when creating a user with duplicate email"
    ) {
      for {
        (user, userId) <- setupUser
        duplicateUser = user.copy(phoneNumber = UsersFixtures.nextPhoneNumber())
        createTwo <- usersRepository.safeCreateUser(
          duplicateUser.userTypeId,
          duplicateUser.firstName,
          duplicateUser.lastName,
          duplicateUser.email,
          duplicateUser.phoneNumber,
          duplicateUser.passwordHash
        )
      } yield assertTrue({
        val error = createTwo.left
        error.getOrElse(throw new Exception()) == UserAlreadyExists()
      })
    }
  ) @@ TestAspect.beforeAll(initializeDb)
    @@ TestAspect.afterAll(closeDb)
}
