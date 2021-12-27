import sbt._

object Dependencies {

  object V {
    val tapir = "0.19.1"
    val r4c = "1.0.0"
    val doobie = "1.0.0-RC1"
    val logback = "1.2.7"
  }

  lazy val tapir4zio = Seq(
    "com.softwaremill.sttp.tapir" %% "tapir-zio" % "0.19.1",
    "com.softwaremill.sttp.tapir" %% "tapir-zio-http4s-server" % "0.19.1"
  )

  lazy val redis4cats = Seq("dev.profunktor" %% "redis4cats-effects" % V.r4c)
  lazy val doobie = Seq(
    "org.tpolecat" %% "doobie-core" % V.doobie,
    "org.tpolecat" %% "doobie-postgres" % V.doobie
  )

  lazy val `tapir-circe` = Seq(
    "com.softwaremill.sttp.client3" %% "circe" % "3.3.16",
    "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % V.tapir
  )

  lazy val swagger = Seq(
    "com.softwaremill.sttp.tapir" %% "tapir-openapi-docs" % V.tapir,
    "com.softwaremill.sttp.tapir" %% "tapir-openapi-circe-yaml" % V.tapir,
    "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui" % V.tapir,
    "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % V.tapir
  )

  lazy val derevo = Seq("tf.tofu" %% "derevo-circe" % "0.12.7")

  lazy val logback = Seq(
    "ch.qos.logback" % "logback-classic" % V.logback % Runtime,
    "ch.qos.logback" % "logback-core" % V.logback % Runtime
  )

  lazy val slf4j = Seq("org.slf4j" % "slf4j-api" % "1.7.32")

  lazy val enumeratum = Seq("com.beachape" %% "enumeratum-circe" % "1.7.0")

  lazy val mouse = Seq("org.typelevel" %% "mouse" % "1.0.7")


  lazy val authDependencies =
    tapir4zio ++
      redis4cats ++
      `tapir-circe` ++
      derevo ++
      logback ++
      slf4j ++
      swagger ++
      enumeratum ++
      mouse


}
