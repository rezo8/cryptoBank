package httpServer

import cats.effect.IO
import models.User
import repository.UsersRepository
import zio.json.*
import zio.{IO, ZIO}
import zio.http.*
import zio._
import zio.http._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import java.util.UUID
import zio.interop.catz.*
import cats.effect.unsafe.implicits.global
import zio.*

import scala.concurrent.{ExecutionContext, Future}

abstract class UserRoutes extends RouteContainer {
  val usersRepository: UsersRepository
  implicit val ec: ExecutionContext =
    scala.concurrent.ExecutionContext.Implicits.global
  private val rootUrl = "users"

  // Simulate an async database call using Future
  def getUserById(userId: Int): Future[String] = Future {
    Thread.sleep(500) // Simulate delay
    s"User details for ID: $userId"
  }

  val routes: HttpApp[Any, Throwable] = Http.collectZIO[Request](
    Method.POST / Root -> {
      handler { (req: Request) =>
        {
          ZIO
            .fromFuture(implicit ec => getUserById(1))
            .map(Response.text)
            .catchAll(error => {
              ZIO.succeed(Response.internalServerError(error.getMessage))
            })
        }
      }
    }
  )

//
//  override def routes: Seq[Route[Any, Exception]] = Seq(
//    (Method.POST / rootUrl -> handler { (req: Request) =>
//      val createResp = for {
//        userBodyString <- req.body.asString
//        user <- ZIO
//          .fromEither(userBodyString.fromJson[User])
//          .mapError(e => new RuntimeException(s"Invalid User JSON: $e"))
//        createRes <- handleCreateUser(user)
////        successfulCreate <- ZIO.fromEither(createRes)
//      } yield createRes
//
//      createResp.flatMap(x => {
//        x.fold(error => {
//          Response.internalServerError(error)
//        })
//      })
//    })
//  )

  private def handleCreateUser(user: User) = {
    usersRepository
      .safeCreateUser(user)
      .to[Task]
  }
}
