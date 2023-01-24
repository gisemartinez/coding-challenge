
val Http4sVersion = "0.21.5"
val CirceVersion = "0.13.0"
val LogbackVersion = "1.2.3"
val ScalaTestVersion = "3.2.0"
val log4catsVersion = "1.0.1"
val SlickVersion = "3.4.1"
val zioVersion = "1.0.13"
val sttpVersion = "3.3.18"

lazy val root = (project in file("."))
  .settings(
    organization := "co.theorg",
    name := "backend",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "2.13.8",
    libraryDependencies ++= Seq(
      "org.scala-lang.modules" %% "scala-parser-combinators" % "2.1.1",
      "org.http4s" %% "http4s-blaze-server" % Http4sVersion,
      "org.http4s" %% "http4s-blaze-client" % Http4sVersion,
      "org.http4s" %% "http4s-circe" % Http4sVersion,
      "org.http4s" %% "http4s-dsl" % Http4sVersion,
      "io.circe" %% "circe-generic" % CirceVersion,
      "io.circe" %% "circe-optics" % CirceVersion,
      "com.typesafe.slick" %% "slick" % SlickVersion,
      "com.typesafe.slick" %% "slick-hikaricp" % SlickVersion,
      "org.postgresql" % "postgresql" % "42.3.4",
      "com.typesafe.slick" %% "slick-hikaricp" % SlickVersion,
      "com.typesafe.slick" %% "slick-codegen" % SlickVersion,
      "org.sangria-graphql" %% "sangria" % "3.4.1",
      "org.sangria-graphql" %% "sangria-circe" % "1.3.1",
      "io.chrisdavenport" %% "log4cats-slf4j" % log4catsVersion,
      "ch.qos.logback" % "logback-classic" % LogbackVersion,
      "org.scalactic" %% "scalactic" % ScalaTestVersion,
      "org.scalatest" %% "scalatest" % ScalaTestVersion % "test",
      "dev.zio" %% "zio" % zioVersion % "test",
      "dev.zio" %% "zio-test" % zioVersion % "test",
      "dev.zio" %% "zio-logging-slf4j" % "0.5.14",
      "dev.zio" %% "zio-test-sbt" % zioVersion % "test",
      "dev.zio" %% "zio-config-typesafe" % "1.0.10",
      "io.circe" %% "circe-derivation" % "0.13.0-M5",
      "com.softwaremill.sttp.client3" %% "core" % sttpVersion,
      "com.softwaremill.sttp.client3" %% "httpclient-backend-zio" % sttpVersion,
      "com.softwaremill.sttp.client3" %% "circe" % sttpVersion
    ),
    addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.10.3")
  )
Compile / mainClass := Some("co.theorg.api.Main")
packageBin / mainClass := Some("co.theorg.api.Main")

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding",
  "UTF-8",
  "-language:higherKinds",
  "-language:postfixOps",
  "-feature",
  "-Xlint:unused"
)

enablePlugins(DockerPlugin)
enablePlugins(UniversalPlugin)
enablePlugins(JavaAppPackaging)


import slick.codegen.SourceCodeGenerator
import slick.model
//Slick Code Generation
enablePlugins(CodegenPlugin)
slickCodegenDatabaseUrl := "jdbc:postgresql://localhost:5438/postgres"
slickCodegenDatabaseUser := "postgres"
slickCodegenDatabasePassword := "postgres"
slickCodegenDriver := slick.jdbc.PostgresProfile
slickCodegenJdbcDriver := "org.postgresql.Driver"
slickCodegenOutputPackage := "co.theorg.db"
slickCodegenCodeGenerator := { (slickModel: model.Model) => new SourceCodeGenerator(slickModel) }

dockerExposedPorts := Seq(8080)
