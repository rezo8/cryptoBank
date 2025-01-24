package repository

import cats.effect.IO
import components.DbMigrationComponent
import config.DatabaseConfig
import doobie.util.transactor.Transactor
import doobie.util.transactor.Transactor.Aux
import org.flywaydb.core.api.output.MigrateResult
import org.testcontainers.containers.PostgreSQLContainer
import zio.ZIO

// TODO consider creating an extendable clean and setup method so that we can run them after and before each test.
// Will keep DB clean and DRY up code by allowing dependent db elements to be set up.
trait RepositorySpec extends DbMigrationComponent {

  private val container = new PostgreSQLContainer("postgres:latest")
  container.start()

  override val dbConfig: DatabaseConfig = DatabaseConfig(
    url = container.getJdbcUrl,
    user = container.getUsername,
    password = container.getPassword
  )

  protected def testTransactor: Aux[IO, Unit] =
    Transactor.fromDriverManager[IO](
      driver = "org.postgresql.Driver",
      url = dbConfig.url,
      user = dbConfig.user,
      password = dbConfig.password,
      logHandler = None
    )

  def initializeDb: ZIO[Any, Nothing, MigrateResult] = flyWayInitialize()

  def closeDb: ZIO[Any, Nothing, Unit] = ZIO.succeed(container.close())
}
