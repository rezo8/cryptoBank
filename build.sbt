import sbtassembly.MergeStrategy
import sbtassembly.PathList

val scala3Version = "3.6.2"
val zioVersion = "2.1.14"

ThisBuild / scalaVersion := scala3Version

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
      "org.scalatest" %% "scalatest" % "3.2.19" % Test,
      "org.scalatestplus" %% "scalacheck-1-17" % "3.2.18.0" % Test,

      // Database
      "org.testcontainers" % "postgresql" % "1.20.4" % Test,
      "org.scalikejdbc" %% "scalikejdbc" % "4.3.2",

      // config
      "com.typesafe" % "config" % "1.4.3",
      "com.github.pureconfig" %% "pureconfig-core" % "0.17.8",
      // Flyway
      "org.flywaydb" % "flyway-core" % "11.2.0",
      "org.flywaydb" % "flyway-database-postgresql" % "11.2.0" % "runtime",

      // Zio
      "dev.zio" %% "zio" % zioVersion,
      "dev.zio" %% "zio-http" % "3.0.1",
      "dev.zio" %% "zio-interop-cats" % "23.1.0.3",
      "dev.zio" %% "zio-json" % "0.7.4",
      "dev.zio" %% "zio-test" % zioVersion % Test,
      "dev.zio" %% "zio-test-sbt" % zioVersion % Test,
      "dev.zio" %% "zio-test-magnolia" % zioVersion % Test,
      "dev.zio" %% "zio-test-junit" % zioVersion % Test,

      // cats
      "org.typelevel" %% "cats-effect-testing-scalatest" % "1.6.0"
    ),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
  )

enablePlugins(FlywayPlugin)

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case "application.conf"            => MergeStrategy.concat
  case "reference.conf"              => MergeStrategy.concat
  case _                             => MergeStrategy.first
}
