import _root_.config.{
  AppConfig,
  ConfigLoadException,
  DatabaseConfig,
  DerivedConfig
}
import components.DbMigrationComponent
import httpServer.BaseServer
import pureconfig.ConfigSource
import zio.{ZIO, ZIOAppDefault}

object Main extends ZIOAppDefault with DbMigrationComponent with BaseServer {

  val config = ConfigSource.default
    .at("app")
    .load[DerivedConfig]
    .getOrElse(throw new ConfigLoadException())
    .asInstanceOf[AppConfig]

  override val dbConfig: DatabaseConfig = config.database

  private def appLogic = {
    for {
      a <- this.flyWayInitialize()
    } yield ()
    startServer
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
