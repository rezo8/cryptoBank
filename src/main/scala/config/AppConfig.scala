package config

import pureconfig.*
import pureconfig.generic.derivation.*

sealed trait DerivedConfig derives ConfigReader
case class AppConfig(
    database: DatabaseConfig
) extends DerivedConfig

case class DatabaseConfig(
    url: String,
    user: String,
    password: String
)

class ConfigLoadException extends Exception
