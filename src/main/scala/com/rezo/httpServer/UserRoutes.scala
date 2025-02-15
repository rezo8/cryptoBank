package com.rezo.httpServer

import Requests.CreateUserRequest
import Responses.LoadUserResponse
import com.rezo.events.CreateUserEvent
import com.rezo.kafka.producers.CreateUserEventProducer
import com.rezo.models.{User, UserType}
import com.rezo.services.UsersService
import org.mindrot.jbcrypt.BCrypt
import zio.*
import zio.http.*

import java.util
import java.util.UUID
import scala.concurrent.ExecutionContext

abstract class UserRoutes extends RouteContainer {
  val usersService: UsersService
  //  val createUserEventProducer: CreateUserEventProducer
  implicit val ec: ExecutionContext
  private val rootUrl = "users"

  override val routes: Routes[Any, Response] = Routes(
    Method.POST / "users" -> handler { handleCreateUser(_) },
    Method.GET / "users" / "email" / string("email") -> handler {
      (email: String, _: Request) => handleLoadByEmail(email)
    },
    Method.GET / "users" / "id" / zio.http.uuid("eventId") -> handler {
      (id: UUID, _: Request) => handleLoadById(id)
    }
  )

  private def handleLoadByEmail(email: String): ZIO[Any, Nothing, Response] = {
    handleServerResponse[LoadUserResponse](for {
      loadRes <- usersService.getUserByEmail(email)
    } yield LoadUserResponse.fromUser(loadRes))
  }

  private def handleLoadById(id: UUID): ZIO[Any, Nothing, Response] = {
    handleServerResponse[LoadUserResponse](for {
      loadRes <- usersService.getUserById(id)
    } yield LoadUserResponse.fromUser(loadRes))
  }

  private def handleCreateUser(
      req: Request
  ): ZIO[Any, Nothing, Response] = {
    handleServerResponseWithRequest[CreateUserRequest, LoadUserResponse](
      req,
      (createUserRequest: CreateUserRequest) => {
        usersService
          .createUser(
            userTypeId = UserType.intFromString(createUserRequest.userType),
            firstName = createUserRequest.firstName,
            lastName = createUserRequest.lastName,
            email = createUserRequest.email,
            phoneNumber = createUserRequest.phoneNumber,
            passwordHash =
              BCrypt.hashpw(createUserRequest.password, BCrypt.gensalt())
          )
          // TODO have create user return user so we can do the create event.
          .map(userId =>
            LoadUserResponse(
              userId,
              createUserRequest.firstName,
              createUserRequest.lastName,
              createUserRequest.email,
              createUserRequest.phoneNumber
            )
          )
      }
    )
  }
}
