val scala3Version = "3.6.2"

lazy val root = project
  .in(file("."))
  .settings(
    name := "bitcoin",
    version := "0.1.0-SNAPSHOT",
    scalaVersion := scala3Version,
    libraryDependencies ++= Seq(
      // Doobie Imports
      "org.tpolecat" %% "doobie-core" % "1.0.0-RC6",
      "org.tpolecat" %% "doobie-postgres" % "1.0.0-RC6",
      "org.tpolecat" %% "doobie-hikari" % "1.0.0-RC6",

      // Scalactic imports
      "org.scalactic" %% "scalactic" % "3.2.19",
      "org.scalatest" %% "scalatest" % "3.2.19" % "test",
      "org.scalameta" %% "munit" % "1.0.4" % Test,

      // Database
      "org.scalikejdbc" %% "scalikejdbc" % "4.3.2",

      // config
      "com.typesafe" % "config" % "1.4.3",
      "com.github.pureconfig" %% "pureconfig-core" % "0.17.8",
      // Flyway
      "org.flywaydb" % "flyway-core" % "10.4.1",
      "org.flywaydb" % "flyway-database-postgresql" % "10.4.1" % "runtime",

      // Zio
      "dev.zio" %% "zio" % "2.1.14",
      "dev.zio" %% "zio-http" % "3.0.1",

// cats
      "org.typelevel" %% "cats-effect-testing-scalatest" % "1.6.0"
    )
  )

enablePlugins(FlywayPlugin)

flywayDriver := "org.postgresql.Driver"
flywayUrl := "jdbc:postgresql://localhost:5432/postgres"
flywayUser := "myuser"
flywayPassword := "postgres"
flywayLocations := Seq("classpath:db/migration")
