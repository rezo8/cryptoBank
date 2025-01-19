package httpServer

import zio.http.*
import zio.{ZIO, ZIOAppDefault, ZLayer}

trait BaseServer extends ZIOAppDefault {
  val userRoutes: UserRoutes

  def startServer = {
    println("serving")
    Server
      .serve(userRoutes.routes)
      .provide(
        Server.live,
        ZLayer.succeed(Server.Config.default.port(8080))
      )
  }
}
