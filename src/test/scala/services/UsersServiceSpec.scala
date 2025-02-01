package services

import models.User
import repository.Exceptions.{
  MissingUserByEmail,
  MissingUserById,
  UnexpectedError,
  UniqueViolationUser
}
import repository.UsersRepositoryTrait
import repository.mocks.UsersRepositoryMock
import services.Exceptions.{DatabaseConflict, MissingDatabaseObject, Unexpected}
import zio.*
import zio.mock.Expectation.*
import zio.test.*
import zio.test.Assertion.*

import java.time.Instant
import java.util.UUID

object UsersServiceSpec extends ZIOSpecDefault {

  val userId: UUID = UUID.randomUUID()
  val userTypeId = 1
  val firstName = "John"
  val lastName = "Doe"
  val email = "john.doe@example.com"
  val phoneNumber = "1234567890"
  val passwordHash = "hash"
  val user: User = User(
    Some(userId),
    userTypeId,
    firstName,
    lastName,
    email,
    phoneNumber,
    passwordHash,
    Instant.now,
    Instant.now
  )
  def spec: Spec[Any, Throwable] = suite("UsersServiceSpec")(
    suite("#createUser") {
      val program = for {
        usersService <- ZIO.service[UsersService]
        result <- usersService.createUser(
          userTypeId,
          firstName,
          lastName,
          email,
          phoneNumber,
          passwordHash
        )
      } yield result
      Seq(
        test("returns a UUID on success") {
          val mockEnv: ULayer[UsersRepositoryTrait] =
            UsersRepositoryMock
              .CreateUser(
                equalTo(
                  (
                    userTypeId,
                    firstName,
                    lastName,
                    email,
                    phoneNumber,
                    passwordHash
                  )
                ),
                value(userId)
              )
              .toLayer

          assertZIO(program.provideLayer(mockEnv >>> UsersService.live))(
            equalTo(userId)
          )
        },
        test("fails with UniqueViolation response on UniqueViolationUser") {
          val uniqueViolationUser = UniqueViolationUser(email, phoneNumber)
          val mockEnv: ULayer[UsersRepositoryTrait] =
            UsersRepositoryMock
              .CreateUser(
                equalTo(
                  (
                    userTypeId,
                    firstName,
                    lastName,
                    email,
                    phoneNumber,
                    passwordHash
                  )
                ),
                failure(uniqueViolationUser)
              )
              .toLayer

          assertZIO(program.exit)(
            fails(
              equalTo(DatabaseConflict(uniqueViolationUser.getMessage))
            )
          ).provideLayer(mockEnv >>> UsersService.live)
        },
        test("fails with Unexpected response on unexpected error") {
          val randomException = UnexpectedError("test")

          val mockEnv: ULayer[UsersRepositoryTrait] =
            UsersRepositoryMock
              .CreateUser(
                equalTo(
                  (
                    userTypeId,
                    firstName,
                    lastName,
                    email,
                    phoneNumber,
                    passwordHash
                  )
                ),
                failure(randomException)
              )
              .toLayer

          assertZIO(program.exit)(fails(equalTo(Unexpected(randomException))))
            .provideLayer(mockEnv >>> UsersService.live)
        }
      )
    },
    suite("getUserByEmail") {
      val email = "john.doe@example.com"
      val program = for {
        usersService <- ZIO.service[UsersService]
        result <- usersService.getUserByEmail(email)
      } yield result
      Seq(
        test("returns a User on success") {
          val mockEnv: ULayer[UsersRepositoryTrait] =
            UsersRepositoryMock
              .GetUserByEmail(equalTo(email), value(user))
              .toLayer

          assertZIO(program.provideLayer(mockEnv >>> UsersService.live))(
            equalTo(user)
          )
        },
        test("fails with MissingDatabaseObject on MissingUserByEmail error") {
          val missingUserByEmail = MissingUserByEmail(email)
          val mockEnv: ULayer[UsersRepositoryTrait] =
            UsersRepositoryMock
              .GetUserByEmail(
                equalTo(email),
                failure(missingUserByEmail)
              )
              .toLayer

          assertZIO(program.exit)(
            fails(
              equalTo(MissingDatabaseObject(missingUserByEmail.getMessage))
            )
          ).provideLayer(mockEnv >>> UsersService.live)
        },
        test("fails with Unexpected response on unexpected error") {
          val randomException = UnexpectedError("test")
          val mockEnv: ULayer[UsersRepositoryTrait] =
            UsersRepositoryMock
              .GetUserByEmail(
                equalTo(email),
                failure(randomException)
              )
              .toLayer

          assertZIO(program.exit)(fails(equalTo(Unexpected(randomException))))
            .provideLayer(mockEnv >>> UsersService.live)
        }
      )
    },
    suite("getUserById") {
      val program = for {
        usersService <- ZIO.service[UsersService]
        result <- usersService.getUserById(userId)
      } yield result
      Seq(
        test("returns a User on success") {
          val mockEnv: ULayer[UsersRepositoryTrait] =
            UsersRepositoryMock
              .GetUser(equalTo(userId), value(user))
              .toLayer

          assertZIO(program.provideLayer(mockEnv >>> UsersService.live))(
            equalTo(user)
          )
        },
        test("fails with MissingDatabaseObject on MissingUserByEmail error") {
          val missingUserById = MissingUserById(userId)
          val mockEnv: ULayer[UsersRepositoryTrait] =
            UsersRepositoryMock
              .GetUser(equalTo(userId), failure(missingUserById))
              .toLayer

          assertZIO(program.exit)(
            fails(
              equalTo(MissingDatabaseObject(missingUserById.getMessage))
            )
          ).provideLayer(mockEnv >>> UsersService.live)
        },
        test("fails with Unexpected response on unexpected error") {
          val randomException = UnexpectedError("test")
          val mockEnv: ULayer[UsersRepositoryTrait] =
            UsersRepositoryMock
              .GetUser(equalTo(userId), failure(randomException))
              .toLayer

          assertZIO(program.exit)(
            fails(
              equalTo(Unexpected(randomException))
            )
          ).provideLayer(mockEnv >>> UsersService.live)
        }
      )
    }
  )
}
