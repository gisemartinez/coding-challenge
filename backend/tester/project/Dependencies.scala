import sbt._

object Dependencies {

  val calibanVersion = "1.3.1"
  val sttpVersion = "3.3.18"
  val zioVersion = "1.0.13"

  case object `dev.zio` {
    case object zio {
      val zio = "dev.zio" %% "zio" % zioVersion
      val test = "dev.zio" %% "zio-test" % zioVersion % "test"
      val `logging-slf4j` = "dev.zio" %% "zio-logging-slf4j" % "0.5.14"
      val `test-sbt` = "dev.zio" %% "zio-test-sbt" % zioVersion % "test"
      val `config_typesafe` =
        "dev.zio" %% "zio-config-typesafe" % "1.0.10"
    }
  }

  case object `ch.qos.logback` {
    val `logback-classic` = "ch.qos.logback" % "logback-classic" % "1.2.7"
  }


  case object `com.github.ghostdogpr` {
    val caliban = "com.github.ghostdogpr" %% "caliban" % calibanVersion
    val client = "com.github.ghostdogpr" %% "caliban-client" % calibanVersion
  }

  case object `com.softwaremill.sttp.client3` {

    val core = "com.softwaremill.sttp.client3" %% "core" % sttpVersion
    val `httpclient-backend-zio` = "com.softwaremill.sttp.client3" %% "httpclient-backend-zio" % sttpVersion
  }
}
