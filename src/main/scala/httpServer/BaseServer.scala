package httpServer

import zio.http.*
import zio.{ZIO, ZIOAppDefault, ZLayer}

trait BaseServer extends ZIOAppDefault {
  val userRoutes: UserRoutes
  val walletsRoutes: WalletsRoutes

  def startServer: ZIO[Any, Throwable, Nothing] = {
    println("serving")
    Server
      .serve(userRoutes.routes.++(walletsRoutes.routes))
      .provide(
        Server.live,
        ZLayer.succeed(Server.Config.default.port(8080))
      )
  }
}
