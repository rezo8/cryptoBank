package httpServer

import zio.http.*
import zio.{ZIO, ZIOAppDefault, ZLayer}

trait BaseServer extends ZIOAppDefault {
  val addressesRoutes: AddressesRoutes
  val userRoutes: UserRoutes
  val accountsRoutes: AccountsRoutes

  def startServer: ZIO[Any, Throwable, Nothing] = {
    println("serving")
    Server
      .serve(
        addressesRoutes.routes.++(userRoutes.routes).++(accountsRoutes.routes)
      )
      .provide(
        Server.live,
        ZLayer.succeed(Server.Config.default.port(8080))
      )
  }
}
