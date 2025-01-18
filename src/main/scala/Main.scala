import _root_.config.{
  AppConfig,
  ConfigLoadException,
  DatabaseConfig,
  DerivedConfig
}
import components.DbMigrationComponent
import pureconfig.ConfigSource
import zio.{ZIO, ZIOAppDefault}

object Main extends ZIOAppDefault with DbMigrationComponent {

  val config = ConfigSource.default
    .at("app")
    .load[DerivedConfig]
    .getOrElse(throw new ConfigLoadException())
    .asInstanceOf[AppConfig]

  override val dbConfig: DatabaseConfig = config.database

  override def run: ZIO[Any, Nothing, Unit] = {
    for {
      a <- this.flyWayInitialize()
      _ = println("ran flyway")
    } yield ()
  }
}
