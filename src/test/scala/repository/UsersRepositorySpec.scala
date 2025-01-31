package repository

import cats.effect.*
import doobie.util.transactor.Transactor.Aux
import fixtures.UsersFixtures
import repository.Exceptions.{
  MissingUserByEmail,
  MissingUserById,
  UniqueViolationUser
}
import repository.UsersRepositorySpec.test
import zio.ZIO
import zio.test.*
import zio.test.Assertion.*

import java.util.UUID

object UsersRepositorySpec extends ZIOSpecDefault with RepositorySpec {
  val usersRepository: UsersRepository = new UsersRepository {
    override val transactor: Aux[IO, Unit] = testTransactor
  }

  private def setupUser = for {
    user <- ZIO.succeed(UsersFixtures.nextUser())
    userId <- usersRepository.createUser(
      user.userTypeId,
      user.firstName,
      user.lastName,
      user.email,
      user.phoneNumber,
      user.passwordHash
    )
  } yield (user, userId)

  def spec: Spec[Any, Throwable] = suite("UsersRepositorySpec")(
    test("properly create and load user by id") {
      for {
        (user, userId) <- setupUser
        loadedUser <- usersRepository.getUser(userId)
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
        loadedUser <- usersRepository.getUserByEmail(user.email)
      } yield assertTrue(
        user.copy(
          userId = Some(userId),
          createdAt = loadedUser.createdAt,
          updatedAt = loadedUser.updatedAt
        ) == loadedUser
      )
    },
    test(
      "fails with MissingUserById when user does not exist when loading by id"
    ) {
      val randomId = UUID.randomUUID()
      assertZIO(usersRepository.getUser(randomId).exit)(
        fails(equalTo(MissingUserById(randomId)))
      )
    },
    test(
      "fails with MissingUserByEmail when user does not exist when loading by email"
    ) {
      val randomEmail = "invalid email"
      assertZIO(usersRepository.getUserByEmail(randomEmail).exit)(
        fails(equalTo(MissingUserByEmail(randomEmail)))
      )
    },
    test(
      "fails with UniqueViolationUser when creating a user with duplicate email"
    ) {
      for {
        (user, userId) <- setupUser
        duplicateUser = user.copy(phoneNumber = UsersFixtures.nextPhoneNumber())
        test <- assertZIO(
          usersRepository
            .createUser(
              duplicateUser.userTypeId,
              duplicateUser.firstName,
              duplicateUser.lastName,
              duplicateUser.email,
              duplicateUser.phoneNumber,
              duplicateUser.passwordHash
            )
            .exit
        )({
          fails(
            equalTo(
              UniqueViolationUser(
                duplicateUser.email,
                duplicateUser.phoneNumber
              )
            )
          )
        })
      } yield test
    }
  ) @@ TestAspect.beforeAll(initializeDb)
    @@ TestAspect.afterAll(closeDb)
}
