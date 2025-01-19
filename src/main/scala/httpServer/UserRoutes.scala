package httpServer

import cats.effect.unsafe.implicits.global
import httpServer.Requests.{CreateUserRequest, LoadUserByEmailRequest}
import models.User
import models.User.encoder
import repository.Exceptions.ServerException
import repository.UsersRepository
import zio.*
import zio.http.*
import zio.interop.catz.*
import zio.json.*

import java.util
import java.util.UUID
import scala.concurrent.ExecutionContext

abstract class UserRoutes extends RouteContainer {
  val usersRepository: UsersRepository
  implicit val ec: ExecutionContext =
    scala.concurrent.ExecutionContext.Implicits.global
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
    handleRepositoryProcess[User](for {
      loadRes <- usersRepository.getUserByEmail(email).to[Task]
    } yield loadRes)
  }

  private def handleLoadById(id: UUID): ZIO[Any, Nothing, Response] = {
    handleRepositoryProcess[User](for {
      loadRes <- usersRepository.getUser(id).to[Task]
    } yield loadRes)
  }

  private def handleCreateUser(
      req: Request
  ): ZIO[Any, Nothing, Response] = {
    handleRepositoryProcess[UUID](for {
      userBodyString <- req.body.asString
      userRequest <- ZIO.fromEither(userBodyString.fromJson[CreateUserRequest])
      createRes <- usersRepository
        .safeCreateUser(userRequest.toUser())
        .to[Task]
    } yield createRes)
  }

  private def loadUserByEmail(req: Request): ZIO[Any, Nothing, Response] = {
    handleRepositoryProcess[User](for {
      userBodyString <- req.body.asString
      userRequest <- ZIO.fromEither(
        userBodyString.fromJson[LoadUserByEmailRequest]
      )
      createRes <- usersRepository
        .getUserByEmail(userRequest.email)
        .to[Task]
    } yield createRes)
  }

  private def handleRepositoryProcess[A](
      repoProc: ZIO[Any, Serializable, Either[ServerException, A]]
  )(implicit enc: zio.json.JsonEncoder[A]): ZIO[Any, Nothing, Response] = {
    repoProc.fold(
      err => {
        println(err) // TODO add logging
        Response.internalServerError("unexpected error")
      },
      success => {
        success.fold(
          error =>
            Response.error(status = error.status, message = error.getMessage),
          success => {
            Response.text(success.toJson)
          }
        )
      }
    )
  }
}
