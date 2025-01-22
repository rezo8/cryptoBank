import config.{AppConfig, ConfigLoadException, DatabaseConfig, DerivedConfig}
import pureconfig.ConfigSource

package object repository {
  val testDbConfig: DatabaseConfig = ConfigSource.default
    .at("app")
    .load[DerivedConfig]
    .getOrElse(throw new ConfigLoadException())
    .asInstanceOf[AppConfig]
    .database
}
