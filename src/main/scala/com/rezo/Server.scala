package com.rezo

import cats.effect
import cats.effect.IO
import com.rezo.Server.startServer
import com.rezo.config.{AppConfig, ConfigLoadException, DerivedConfig}
import com.rezo.httpServer.{
  AccountsRoutes,
  AddressesRoutes,
  BaseServer,
  UserRoutes
}
import com.rezo.repository.{
  AccountsRepository,
  AddressesRepository,
  UsersRepository
}
import com.rezo.services.{AccountsService, AddressesService, UsersService}
import doobie.*
import doobie.util.transactor.Transactor
import doobie.util.transactor.Transactor.Aux
import pureconfig.ConfigSource
import zio.*

import scala.concurrent.ExecutionContext

object Server extends ZIOAppDefault with BaseServer {
  main =>

  val config: AppConfig = ConfigSource.default
    .at("app")
    .load[DerivedConfig]
    .getOrElse(throw new ConfigLoadException())
    .asInstanceOf[AppConfig]

  private val transactor =
    Transactor.fromDriverManager[IO](
      driver = "org.postgresql.Driver",
      url = config.database.url,
      user = config.database.user,
      password = config.database.password,
      logHandler = None
    )

  // Repositories
  private val addressRepository = new AddressesRepository(main.transactor)

  private val usersRepository = new UsersRepository(main.transactor)

  private val accountsRepository = new AccountsRepository(main.transactor)

  // Routes
  override val addressesRoutes: AddressesRoutes = new AddressesRoutes:
    override val addressesService: AddressesService =
      AddressesService(main.addressRepository)

    override implicit val ec: ExecutionContext =
      scala.concurrent.ExecutionContext.Implicits.global

  override val userRoutes: UserRoutes = new UserRoutes:
    override val usersService: UsersService = UsersService(main.usersRepository)
    override implicit val ec: ExecutionContext =
      scala.concurrent.ExecutionContext.Implicits.global

  override val accountsRoutes: AccountsRoutes = new AccountsRoutes:
    override val accountsService: AccountsService =
      AccountsService(main.accountsRepository)
    override implicit val ec: ExecutionContext =
      scala.concurrent.ExecutionContext.Implicits.global

  private def appLogic: ZIO[Any, Throwable, Nothing] = {
    for {
      serverProc <- startServer
    } yield serverProc
  }

  private def cleanup = {
    // Perform cleanup operations here
    // Fortunately ZIO Http Server comes with graceful shutdown built in: https://github.com/zio/zio-http/pull/2099/files
    println("shutting down")
    ZIO.unit
  }

  override def run: ZIO[Any, Throwable, Int] = {
    appLogic.ensuring(cleanup)
  }
}
