package httpServer

import cats.effect.unsafe.implicits.global
import httpServer.Requests.{CreateUserRequest, LoadUserByEmailRequest}
import models.User.encoder
import repository.UsersRepository
import zio.*
import zio.http.{_}
import zio.http.endpoint.openapi.JsonSchema.StringFormat.{UUID => ZIOUuid}
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
    Method.POST / "users" -> handler { (request: Request) =>
      handleCreateUser(request)
    },
    Method.GET / "users" / "email" / string("email") -> handler {
      (email: String, req: Request) =>
        handleLoadByEmail(email)
    },
    Method.GET / "users" / "id" / zio.http.uuid("uuid") -> handler {
      (id: UUID, req: Request) => handleLoadById(id)
    }
  )

  private def handleLoadByEmail(email: String): ZIO[Any, Nothing, Response] = {
    (for {
      loadRes <- usersRepository.getUserByEmail(email).to[Task]
    } yield loadRes).fold(
      err => {
        println(err)
        Response.internalServerError("unexpected error")
      },
      success => {
        success.fold(
          error => {
            Response.error(status = error.status, message = error.getMessage)
          },
          success => { Response.text(success.toJson) }
        )
      }
    )
  }

  private def handleLoadById(id: UUID): ZIO[Any, Nothing, Response] = {
    (for {
      loadRes <- usersRepository.getUser(id).to[Task]
    } yield loadRes).fold(
      err => {
        println(err)
        Response.internalServerError("unexpected error")
      },
      success => {
        success.fold(
          error => {
            Response.error(status = error.status, message = error.getMessage)
          },
          success => {
            Response.text(success.toJson)
          }
        )
      }
    )
  }

  private def handleCreateUser(
      req: Request
  ): ZIO[Any, Nothing, Response] = {
    (for {
      userBodyString <- req.body.asString
      userRequest <- ZIO.fromEither(userBodyString.fromJson[CreateUserRequest])
      createRes <- usersRepository
        .safeCreateUser(userRequest.toUser())
        .to[Task]
    } yield createRes).fold(
      err => {
        println(err) // TODO add logging
        Response.internalServerError("unexpected error")
      },
      success => {
        success.fold(
          error =>
            Response.error(status = error.status, message = error.getMessage),
          success => {
            Response.text(s"User created with UUID [${success.toString}]")
          }
        )
      }
    )
  }

  private def loadUserByEmail(req: Request): ZIO[Any, Nothing, Response] = {
    (for {
      userBodyString <- req.body.asString
      userRequest <- ZIO.fromEither(
        userBodyString.fromJson[LoadUserByEmailRequest]
      )
      createRes <- usersRepository
        .getUserByEmail(userRequest.email)
        .to[Task]
    } yield createRes).fold(
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
