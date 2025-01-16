val scala3Version = "3.6.2"

lazy val root = project
  .in(file("."))
  .settings(
    name := "bitcoin",
    version := "0.1.0-SNAPSHOT",
    scalaVersion := scala3Version,
    libraryDependencies ++= Seq(
      // Doobie Imports
      "org.tpolecat" %% "doobie-core" % "1.0.0-RC4",

      // And add any of these as needed
      "org.tpolecat" %% "doobie-h2" % "1.0.0-RC4", // H2 driver 1.4.200 + type mappings.
      "org.tpolecat" %% "doobie-hikari" % "1.0.0-RC4", // HikariCP transactor.
      "org.tpolecat" %% "doobie-postgres" % "1.0.0-RC4", // Postgres driver 42.6.0 + type mappings.
      "org.tpolecat" %% "doobie-specs2" % "1.0.0-RC4" % "test", // Specs2 support for typechecking statements.
      "org.tpolecat" %% "doobie-scalatest" % "1.0.0-RC4" % "test", // ScalaTest support for typechecking statements.

      // Scalactic imports
      "org.scalactic" %% "scalactic" % "3.2.19",
      "org.scalatest" %% "scalatest" % "3.2.19" % "test",
      "org.scalameta" %% "munit" % "1.0.0" % Test
    )
  )
