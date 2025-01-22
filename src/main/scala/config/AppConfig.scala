package config

import pureconfig.generic.derivation.*
import pureconfig.*

sealed trait DerivedConfig derives ConfigReader
case class AppConfig(
    database: DatabaseConfig
) extends DerivedConfig

case class DatabaseConfig(
    url: String,
    user: String,
    password: String,
    maxPoolSize: Int,
    schema: String
)

class ConfigLoadException extends Exception
