package httpServer

import zio.http.*
import zio.{ZIO, ZIOAppDefault, ZLayer}

trait BaseServer extends ZIOAppDefault {
  val coinsRoutes: CoinsRoutes
  val userRoutes: UserRoutes
  val walletsRoutes: WalletsRoutes

  def startServer: ZIO[Any, Throwable, Nothing] = {
    println("serving")
    Server
      .serve(coinsRoutes.routes.++(userRoutes.routes).++(walletsRoutes.routes))
      .provide(
        Server.live,
        ZLayer.succeed(Server.Config.default.port(8080))
      )
  }
}
