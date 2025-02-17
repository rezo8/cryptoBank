package com.rezo.services

import com.rezo.kafka.producers.CreateUserEventProducerTrait
import com.rezo.kafka.producers.mocks.CreateUserEventProducerMock
import com.rezo.models.User
import com.rezo.repository.Exceptions.{
  MissingUserByEmail,
  MissingUserById,
  UnexpectedError,
  UniqueViolationUser
}
import com.rezo.repository.UsersRepositoryTrait
import com.rezo.repository.mocks.UsersRepositoryMock
import com.rezo.services.Exceptions.{
  DatabaseConflict,
  MissingDatabaseObject,
  Unexpected
}
import org.apache.kafka.clients.producer.RecordMetadata
import org.apache.kafka.common.TopicPartition
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
    userId,
    userTypeId,
    firstName,
    lastName,
    email,
    phoneNumber,
    passwordHash,
    Instant.now,
    Instant.now
  )

  val defaultRecordMetadata = new RecordMetadata(
    new org.apache.kafka.common.TopicPartition("topic", 0),
    0L,
    1,
    0L,
    1,
    1
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
        test("returns the created user on success") {
          val mockRepo: ULayer[UsersRepositoryTrait] =
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
                value(user)
              )
              .toLayer

          val mockSuccessProducer: ULayer[CreateUserEventProducerTrait] =
            CreateUserEventProducerMock
              .Produce(equalTo(user), value(defaultRecordMetadata))
              .toLayer

          val testLayer =
            (mockRepo ++ mockSuccessProducer) >>> UsersService.live

          assertZIO(program.provideLayer(testLayer))(equalTo(user))
        },
        test("fails with UniqueViolation response on UniqueViolationUser") {
          val uniqueViolationUser = UniqueViolationUser(email, phoneNumber)
          val mockRepo: ULayer[UsersRepositoryTrait] =
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

          val mockNoCallProducer: ULayer[CreateUserEventProducerTrait] =
            CreateUserEventProducerMock.empty // Expect no calls to `produce`

          val testLayer =
            (mockRepo ++ mockNoCallProducer) >>> UsersService.live

          assertZIO(program.exit)(
            fails(equalTo(DatabaseConflict(uniqueViolationUser.getMessage)))
          ).provideLayer(testLayer)
        },
        test("fails with Unexpected response on unexpected error") {
          val randomException = UnexpectedError("test")
          val mockRepo: ULayer[UsersRepositoryTrait] =
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

          val mockNoCallProducer: ULayer[CreateUserEventProducerTrait] =
            CreateUserEventProducerMock.empty // Expect no calls to `produce`

          val testLayer =
            (mockRepo ++ mockNoCallProducer) >>> UsersService.live

          assertZIO(program.exit)(
            fails(equalTo(Unexpected(randomException)))
          ).provideLayer(testLayer)
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
          val mockRepo: ULayer[UsersRepositoryTrait] =
            UsersRepositoryMock
              .GetUserByEmail(equalTo(email), value(user))
              .toLayer

          val mockNoCallProducer: ULayer[CreateUserEventProducerTrait] =
            CreateUserEventProducerMock.empty // Expect no calls to `produce`

          val testLayer =
            (mockRepo ++ mockNoCallProducer) >>> UsersService.live

          assertZIO(program.provideLayer(testLayer))(
            equalTo(user)
          )
        },
        test("fails with MissingDatabaseObject on MissingUserByEmail error") {
          val missingUserByEmail = MissingUserByEmail(email)
          val mockRepo: ULayer[UsersRepositoryTrait] =
            UsersRepositoryMock
              .GetUserByEmail(
                equalTo(email),
                failure(missingUserByEmail)
              )
              .toLayer

          val mockNoCallProducer: ULayer[CreateUserEventProducerTrait] =
            CreateUserEventProducerMock.empty // Expect no calls to `produce`

          val testLayer =
            (mockRepo ++ mockNoCallProducer) >>> UsersService.live

          assertZIO(program.exit)(
            fails(equalTo(MissingDatabaseObject(missingUserByEmail.getMessage)))
          ).provideLayer(testLayer)
        },
        test("fails with Unexpected response on unexpected error") {
          val randomException = UnexpectedError("test")
          val mockRepo: ULayer[UsersRepositoryTrait] =
            UsersRepositoryMock
              .GetUserByEmail(
                equalTo(email),
                failure(randomException)
              )
              .toLayer

          val mockNoCallProducer: ULayer[CreateUserEventProducerTrait] =
            CreateUserEventProducerMock.empty // Expect no calls to `produce`

          val testLayer =
            (mockRepo ++ mockNoCallProducer) >>> UsersService.live

          assertZIO(program.exit)(
            fails(equalTo(Unexpected(randomException)))
          ).provideLayer(testLayer)
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
          val mockRepo: ULayer[UsersRepositoryTrait] =
            UsersRepositoryMock
              .GetUser(equalTo(userId), value(user))
              .toLayer

          val mockNoCallProducer: ULayer[CreateUserEventProducerTrait] =
            CreateUserEventProducerMock.empty // Expect no calls to `produce`

          val testLayer =
            (mockRepo ++ mockNoCallProducer) >>> UsersService.live
          assertZIO(program.provideLayer(testLayer))(
            equalTo(user)
          )
        },
        test("fails with MissingDatabaseObject on MissingUserByEmail error") {
          val missingUserById = MissingUserById(userId)
          val mockRepo: ULayer[UsersRepositoryTrait] =
            UsersRepositoryMock
              .GetUser(equalTo(userId), failure(missingUserById))
              .toLayer

          // Mock for "no call" producer (used in all other tests)
          val mockNoCallProducer: ULayer[CreateUserEventProducerTrait] =
            CreateUserEventProducerMock.empty // Expect no calls to `produce`

          val testLayer =
            (mockRepo ++ mockNoCallProducer) >>> UsersService.live

          assertZIO(program.exit)(
            fails(equalTo(MissingDatabaseObject(missingUserById.getMessage)))
          ).provideLayer(testLayer)
        },
        test("fails with Unexpected response on unexpected error") {
          val randomException = UnexpectedError("test")
          val mockRepo: ULayer[UsersRepositoryTrait] =
            UsersRepositoryMock
              .GetUser(equalTo(userId), failure(randomException))
              .toLayer

            // Mock for "no call" producer (used in all other tests)
          val mockNoCallProducer: ULayer[CreateUserEventProducerTrait] =
            CreateUserEventProducerMock.empty // Expect no calls to `produce`

          val testLayer =
            (mockRepo ++ mockNoCallProducer) >>> UsersService.live

          assertZIO(program.exit)(
            fails(equalTo(Unexpected(randomException)))
          ).provideLayer(testLayer)
        }
      )
    }
  )
}
