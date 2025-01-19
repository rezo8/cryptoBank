import _root_.config.{
  AppConfig,
  ConfigLoadException,
  DatabaseConfig,
  DerivedConfig
}
import components.DbMigrationComponent
import httpServer.{BaseServer, UserRoutes}
import pureconfig.ConfigSource
import repository.UsersRepository
import zio.*

object Main extends ZIOAppDefault with DbMigrationComponent with BaseServer {
  main =>

  val config: AppConfig = ConfigSource.default
    .at("app")
    .load[DerivedConfig]
    .getOrElse(throw new ConfigLoadException())
    .asInstanceOf[AppConfig]

  override val dbConfig: DatabaseConfig = config.database

  // Repositories
  private val usersRepository = new UsersRepository:
    override lazy val dbConfig: DatabaseConfig = config.database
  // Routes
  override val userRoutes: UserRoutes = new UserRoutes:
    override val usersRepository: UsersRepository = main.usersRepository

  private def appLogic: ZIO[Any, Throwable, Nothing] = {
    for {
      a <- this.flyWayInitialize()
      x <- startServer
    } yield x
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
