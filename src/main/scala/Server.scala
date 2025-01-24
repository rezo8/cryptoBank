import cats.effect
import cats.effect.IO
import components.DbMigrationComponent
import _root_.config.{
  AppConfig,
  ConfigLoadException,
  DatabaseConfig,
  DerivedConfig
}
import doobie.*
import doobie.util.transactor.Transactor
import doobie.util.transactor.Transactor.Aux
import httpServer.{BaseServer, CoinsRoutes, UserRoutes, WalletsRoutes}
import pureconfig.ConfigSource
import repository.{CoinsRepository, UsersRepository, WalletsRepository}
import zio.*

import scala.concurrent.ExecutionContext

object Server extends ZIOAppDefault with DbMigrationComponent with BaseServer {
  main =>

  val config: AppConfig = ConfigSource.default
    .at("app")
    .load[DerivedConfig]
    .getOrElse(throw new ConfigLoadException())
    .asInstanceOf[AppConfig]

  override val dbConfig: DatabaseConfig = config.database
  private val transactor =
    Transactor.fromDriverManager[IO](
      driver = "org.postgresql.Driver",
      url = dbConfig.url,
      user = dbConfig.user,
      password = dbConfig.password,
      logHandler = None
    )

  // Repositories
  private val coinsRepository = new CoinsRepository {
    override val transactor: Aux[IO, Unit] = main.transactor
  }

  private val usersRepository = new UsersRepository:
    override val transactor: Aux[effect.IO, Unit] = main.transactor

  private val walletsRepository = new WalletsRepository:
    override val transactor: Aux[effect.IO, Unit] = main.transactor

  // Routes
  override val coinsRoutes: CoinsRoutes = new CoinsRoutes:
    override val coinsRepository: CoinsRepository = main.coinsRepository
    override implicit val ec: ExecutionContext =
      scala.concurrent.ExecutionContext.Implicits.global

  override val userRoutes: UserRoutes = new UserRoutes:
    override implicit val ec: ExecutionContext =
      scala.concurrent.ExecutionContext.Implicits.global
    override val usersRepository: UsersRepository = main.usersRepository

  override val walletsRoutes: WalletsRoutes = new WalletsRoutes:
    override val walletsRepository: WalletsRepository = main.walletsRepository
    override implicit val ec: ExecutionContext =
      scala.concurrent.ExecutionContext.Implicits.global

  private def appLogic: ZIO[Any, Throwable, Nothing] = {
    for {
      a <- this.flyWayInitialize()
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
