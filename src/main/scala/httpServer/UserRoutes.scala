package httpServer

import cats.effect.unsafe.implicits.global
import httpServer.Requests.CreateUserRequest
import repository.UsersRepository
import zio.*
import zio.http.*
import zio.interop.catz.*
import zio.json.*

import scala.concurrent.ExecutionContext

abstract class UserRoutes extends RouteContainer {
  val usersRepository: UsersRepository
  implicit val ec: ExecutionContext =
    scala.concurrent.ExecutionContext.Implicits.global
  private val rootUrl = "users"

  override val routes: Routes[Any, Response] = Routes(
    Method.POST / "users" -> handler { (request: Request) =>
      handleCreateUser(request)
    }
  )

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
}
