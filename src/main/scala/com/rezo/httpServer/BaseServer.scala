package com.rezo.httpServer

import com.rezo.config.ServerMetadataConfig
import zio.http.*
import zio.{ZIO, ZIOAppDefault, ZLayer}

trait BaseServer extends ZIOAppDefault {

  val serverMetadataConfig: ServerMetadataConfig

  val addressesRoutes: AddressesRoutes
  val userRoutes: UserRoutes
  val accountsRoutes: AccountsRoutes

  def startServer: ZIO[Any, Throwable, Nothing] = {
    println("serving")
    val config = Server.Config.default.port(serverMetadataConfig.port)
    Server
      .serve(
        addressesRoutes.routes.++(userRoutes.routes).++(accountsRoutes.routes)
      )
      .provide(
        Server.live,
        ZLayer.succeed(config)
      )
  }
}
