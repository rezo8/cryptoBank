package com.rezo.repository

import com.rezo.kafka.producers.CreateUserEventProducer
import com.rezo.repository.Exceptions.{
  MissingUserByEmail,
  MissingUserById,
  UniqueViolationUser
}
import com.rezo.repository.{UsersRepository, UsersRepositoryTrait}
import com.rezo.services.UsersService
import UsersRepositorySpec.test
import com.rezo.fixtures.UsersFixtures
import zio.ZIO
import zio.test.*
import zio.test.Assertion.*

import java.util.UUID

object UsersRepositorySpec extends ZIOSpecDefault with RepositorySpec {
  val usersRepository: UsersRepository = new UsersRepository(testTransactor)

  private def setupUser = for {
    user <- ZIO.succeed(UsersFixtures.nextUser())
    dbUser <- usersRepository.createUser(
      user.userTypeId,
      user.firstName,
      user.lastName,
      user.email,
      user.phoneNumber,
      user.passwordHash
    )
  } yield user

  def spec: Spec[Any, Throwable] = suite("UsersRepositorySpec")(
    suite("getUser")(
      test("properly create and load user by id") {
        for {
          user <- setupUser
          loadedUser <- usersRepository.getUser(user.userId)
        } yield assertTrue(
          user.copy(
            userId = loadedUser.userId,
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
      }
    ),
    suite("getUserByEmail")(
      test("properly create and load user by email") {
        for {
          user <- setupUser
          loadedUser <- usersRepository.getUserByEmail(user.email)
        } yield assertTrue(
          user == loadedUser
        )
      },
      test(
        "fails with MissingUserByEmail when user does not exist when loading by email"
      ) {
        val randomEmail = "invalid email"
        assertZIO(usersRepository.getUserByEmail(randomEmail).exit)(
          fails(equalTo(MissingUserByEmail(randomEmail)))
        )
      }
    ),
    suite("createUser")(
      test(
        "fails with UniqueViolationUser when creating a user with duplicate email"
      ) {
        for {
          user <- setupUser
          duplicateUser = user.copy(phoneNumber =
            UsersFixtures.nextPhoneNumber()
          )
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
    )
  ) @@ TestAspect.beforeAll(initializeDb)
    @@ TestAspect.afterAll(closeDb)
}
