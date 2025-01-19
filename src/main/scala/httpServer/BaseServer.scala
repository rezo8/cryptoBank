package httpServer

import zio.{ZIO, ZIOAppDefault, ZLayer}
import zio.http.*

trait BaseServer extends ZIOAppDefault {
  val routes =
    Routes(
      Method.GET / Root -> handler(Response.text("Greetings at your service")),
      Method.GET / "greet" -> handler { (req: Request) =>
        val name = req.queryParamToOrElse("name", "World")
        Response.text(s"Hello $name!")
      }
    )

  def startServer = {
    println("serving")
    Server
      .serve(routes)
      .provide(
        Server.live,
        ZLayer.succeed(Server.Config.default.port(8080))
      )
  }
}
