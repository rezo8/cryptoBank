import _root_.config.{AppConfig, ConfigLoadException, DerivedConfig}
import cats.effect
import cats.effect.IO
import doobie.*
import doobie.util.transactor.Transactor
import doobie.util.transactor.Transactor.Aux
import httpServer.{AccountsRoutes, AddressesRoutes, BaseServer, UserRoutes}
import pureconfig.ConfigSource
import repository.{AccountsRepository, AddressRepository, UsersRepository}
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
  private val addressRepository = new AddressRepository {
    override val transactor: Aux[IO, Unit] = main.transactor
  }

  private val usersRepository = new UsersRepository:
    override val transactor: Aux[effect.IO, Unit] = main.transactor

  private val accountsRepository = new AccountsRepository:
    override val transactor: Aux[effect.IO, Unit] = main.transactor

  // Routes
  override val addressesRoutes: AddressesRoutes = new AddressesRoutes:
    override val addressRepository: AddressRepository = main.addressRepository
    override implicit val ec: ExecutionContext =
      scala.concurrent.ExecutionContext.Implicits.global

  override val userRoutes: UserRoutes = new UserRoutes:
    override implicit val ec: ExecutionContext =
      scala.concurrent.ExecutionContext.Implicits.global
    override val usersRepository: UsersRepository = main.usersRepository

  override val accountsRoutes: AccountsRoutes = new AccountsRoutes:
    override val accountsRepository: AccountsRepository =
      main.accountsRepository
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
