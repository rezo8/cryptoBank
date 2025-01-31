package services

import org.scalamock.scalatest.MockFactory
import org.scalamock.stubs.Stubs
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import repository.UsersRepository
import repository.UsersRepositorySpec.suite
import zio.*
import zio.test.*
import zio.test.Assertion.*

import java.util.UUID
import scala.annotation.experimental

@experimental
object UsersServiceSpec extends ZIOSpecDefault {

  def spec: Spec[Any, Throwable] = suite("UsersServiceSpec")()

//  val mockUsersRepository: UsersRepository = mock[UsersRepository]
//  val usersService = new UsersService(mockUsersRepository)
//
//  def spec: Spec[Any, Throwable] = suite("UsersServiceSpec")(
//    suite("createUser") {
//      test("succeeds with created user") {
//        val userId = UUID.randomUUID()
//        this.mockUsersRepository.createUser
//          .expects(
//            1,
//            "John",
//            "Doe",
//            "john.doe@example.com",
//            "1234567890",
//            "hashedPassword"
//          )
//          .returning(ZIO.succeed(userId))
//          .once()
//
//        //          .returning(ZIO.succeed(userId))
//        for {
//          result <- usersService.createUser(
//            userTypeId = 1,
//            firstName = "John",
//            lastName = "Doe",
//            email = "john.doe@example.com",
//            phoneNumber = "1234567890",
//            passwordHash = "hashedPassword"
//          )
//        } yield assert(result)(equalTo(userId))
//      }
//    }
//  ) // @@ TestAspect.beforeAll(ZIO.succeed(reset(mockUsersRepository)))
}
