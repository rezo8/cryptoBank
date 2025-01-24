package httpServer

import httpServer.Helpers.handleRepositoryProcess
import httpServer.Requests.CreateUserRequest
import httpServer.Responses.LoadUserResponse
import models.User
import repository.UsersRepository
import zio.*
import zio.http.*
import zio.json.*

import java.util
import java.util.UUID
import scala.concurrent.ExecutionContext

abstract class UserRoutes extends RouteContainer {
  val usersRepository: UsersRepository
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
    handleRepositoryProcess[LoadUserResponse](for {
      loadRes <- usersRepository
        .getUserByEmail(email)
    } yield loadRes.map(LoadUserResponse.fromUser))
  }

  private def handleLoadById(id: UUID): ZIO[Any, Nothing, Response] = {
    handleRepositoryProcess[LoadUserResponse](for {
      loadRes <- usersRepository
        .getUser(id)
    } yield loadRes.map(LoadUserResponse.fromUser))
  }

  private def handleCreateUser(
      req: Request
  ): ZIO[Any, Nothing, Response] = {
    handleRepositoryProcess[LoadUserResponse](for {
      userBodyString <- req.body.asString
      userRequest <- ZIO.fromEither(userBodyString.fromJson[CreateUserRequest])
      createRes <- usersRepository
        .safeCreateUser(userRequest.toUser())
    } yield createRes.map(userId => {
      LoadUserResponse(
        Some(userId),
        userRequest.firstName,
        userRequest.lastName,
        userRequest.email,
        userRequest.phoneNumber
      )
    }))
  }
}
