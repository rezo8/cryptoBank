package httpServer

import httpServer.Helpers.handleServerResponse
import httpServer.Requests.CreateUserRequest
import httpServer.Responses.LoadUserResponse
import models.{User, UserType}
import org.mindrot.jbcrypt.BCrypt
import repository.Exceptions.{ServerException, Unexpected, UnparseableRequest}
import repository.UsersRepository
import services.UsersService
import utils.ZioTypes.RezoTask
import zio.*
import zio.http.*
import zio.json.*

import java.util
import java.util.UUID
import scala.concurrent.ExecutionContext

abstract class UserRoutes extends RouteContainer {
  val usersService: UsersService
  implicit val ec: ExecutionContext
  private val rootUrl = "users"

  override val routes: Routes[Any, Response] = Routes(
    Method.POST / "users" -> handler { handleCreateUser(_) },
    Method.GET / "users" / "email" / string("email") -> handler {
      (email: String, _: Request) => handleLoadByEmail(email)
    },
    Method.GET / "users" / "id" / zio.http.uuid("uuid") -> handler {
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
    handleServerResponse[LoadUserResponse]({
      for {
        userBodyString <- req.body.asString.mapError(Unexpected(_))
        userRequest <- ZIO.fromEither(
          userBodyString
            .fromJson[CreateUserRequest]
            .left
            .map(x => UnparseableRequest(x))
        )
        userId <- usersService.createUser(
          userTypeId = UserType.intFromString(userRequest.userType),
          firstName = userRequest.firstName,
          lastName = userRequest.lastName,
          email = userRequest.email,
          phoneNumber = userRequest.phoneNumber,
          passwordHash = BCrypt.hashpw(userRequest.password, BCrypt.gensalt())
        )
      } yield LoadUserResponse(
        Some(userId),
        userRequest.firstName,
        userRequest.lastName,
        userRequest.email,
        userRequest.phoneNumber
      )
    })
  }
}
