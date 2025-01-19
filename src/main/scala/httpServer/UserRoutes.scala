package httpServer

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import models.User
import repository.UsersRepository
import zio.*
import zio.http.*
import zio.interop.catz.*
import zio.json.*

import scala.concurrent.{ExecutionContext, Future}

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
      user <- ZIO.fromEither(userBodyString.fromJson[User])
      createRes <- usersRepository
        .safeCreateUser(user)
        .to[Task]
    } yield createRes).fold(
      err => {
        println(err)
        Response.internalServerError("unexpected error")
      },
      success => {
        success.fold(
          errorMsg => Response.internalServerError(errorMsg),
          success => {
            Response.text(s"User created with UUID [${success.toString}]")
          }
        )
      }
    )
  }
}
