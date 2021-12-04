name := "cloudProject"


lazy val auth = (project in file("auth"))
  .settings(
    name := "auth",
    version := "0.1" ,
    scalaVersion := "2.13.7"
  )
  .settings(
    libraryDependencies ++= Dependencies.authDependencies
  )
  .settings(
    addCompilerPlugin("org.typelevel" % "kind-projector" % "0.13.2" cross CrossVersion.full)
  )
  .settings(
    scalacOptions += "-Ymacro-annotations"
  )
  .settings(
    Compile / run / mainClass := Some("ru.hes.auth.Main")
  )
  .enablePlugins(JavaAppPackaging)
  .enablePlugins(DockerPlugin)
  .settings(
    dockerBaseImage := "adoptopenjdk:11-jre-hotspot",
    Docker / packageName := "auth-service",
    Docker / version := "0.1",
    dockerExposedPorts := Seq(8080),
    dockerRepository := Some("dsavitsky96"),
    dockerUpdateLatest := true,
    normalizedName := "auth-service",
  )

lazy val shortener = (project in file("shortener"))
  .settings(
    name := "shortener",
    version := "0.1" ,
    scalaVersion := "2.13.7"
  )
  .settings(
    libraryDependencies ++= Dependencies.authDependencies
  )
  .settings(
    scalacOptions += "-Ymacro-annotations"
  )
  .settings(
    Compile / run / mainClass := Some("ru.hes.shortener.Main")
  )
  .settings(
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-dsl" % "0.23.6",
      "org.http4s" %% "http4s-blaze-server" % "0.23.6",
      "org.http4s" %% "http4s-blaze-client" % "0.23.6",
      "org.http4s" %% "http4s-circe" % "0.23.6",
      "org.http4s" %% "rho-swagger" % "0.23.0-RC1",
      "org.http4s" %% "rho-swagger-ui" % "0.23.0-RC1"
    ),
    libraryDependencies += "com.softwaremill.sttp.client3" %% "async-http-client-backend-cats" % "3.3.18",
    libraryDependencies += "com.beachape" %% "enumeratum-circe" % "1.7.0",
    libraryDependencies ++= Dependencies.derevo,
    libraryDependencies ++= Dependencies.redis4cats
  )
  .enablePlugins(JavaAppPackaging)
  .enablePlugins(DockerPlugin)
  .settings(
    dockerBaseImage := "adoptopenjdk:11-jre-hotspot",
    Docker / packageName := "shortener-service",
    Docker / version := "0.1",
    dockerExposedPorts := Seq(8080),
    dockerRepository := Some("dsavitsky96"),
    dockerUpdateLatest := true,
    normalizedName := "auth-service",
  )