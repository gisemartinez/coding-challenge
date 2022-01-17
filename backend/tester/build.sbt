import Dependencies._

lazy val root = (project in file("."))
  .settings(
    name := "tester",
    libraryDependencies ++= Seq(
      `dev.zio`.zio.zio,
      `dev.zio`.zio.test,
      `dev.zio`.zio.`logging-slf4j`,
      `dev.zio`.zio.`test-sbt`,
      `dev.zio`.zio.`config_typesafe`,
      `ch.qos.logback`.`logback-classic`,
      `com.github.ghostdogpr`.caliban,
      `com.github.ghostdogpr`.client,
      `com.softwaremill.sttp.client3`.core % Test,
      `com.softwaremill.sttp.client3`.`httpclient-backend-zio` % Test
    ),
    testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework"))
  )
  .settings(
    Compile / caliban / calibanSources := baseDirectory
      .in(project)
      .value
      .getParentFile
      .getAbsoluteFile / "resources",
    Compile / caliban / calibanSettings += calibanSetting(
      file("backend.graphql")
    )(cs =>
      cs.packageName("io.theorg.tester.client")
        .imports("java.util.UUID", "io.theorg.tester.client.implicits._")
        .scalarMapping(
          "UUID" -> "String"
        )
    )
  )
  .enablePlugins(CalibanPlugin)
