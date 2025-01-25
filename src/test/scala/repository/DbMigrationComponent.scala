package repository

import config.DatabaseConfig
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.output.MigrateResult
import zio.*

trait DbMigrationComponent {
  val dbConfig: DatabaseConfig
  def flyWayInitialize(): ZIO[Any, Nothing, MigrateResult] =
    ZIO.succeed(
      Flyway
        .configure()
        .dataSource(dbConfig.url, dbConfig.user, dbConfig.password)
        .load()
        .migrate()
    )
}
