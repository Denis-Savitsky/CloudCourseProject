package ru.hes.auth

import cats.effect.{ExitCode, IO, IOApp, Resource}
import dev.profunktor.redis4cats.Redis
import dev.profunktor.redis4cats.effect.Log.Stdout.instance
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.server.Router
import ru.hes.auth.api.AuthRoutes
import ru.hes.auth.db.{DB, DBLive}
import ru.hes.auth.generator.TokenGeneratorLive
import ru.hes.auth.service.{AuthService, AuthServiceImpl, AuthServiceLive}
import zio._
import zio.magic._
import zio.blocking.Blocking
import zio.clock.Clock
import zio.interop.catz._
import zio.interop.catz.implicits.rts

object Main extends zio.App {

  val redis = Redis[Task].utf8("redis://:letsrediswithme1@10.20.14.30:6379").toManagedZIO.toLayer

  val service = ZLayer.wire[Has[AuthService]](
    redis,
    DBLive.layer,
    TokenGeneratorLive.layer,
    AuthServiceLive.layer
  )

  val serve: ZIO[Has[AuthService] with Clock with Blocking with zio.ZEnv, Throwable, Unit] = ZIO.runtime[ZEnv].flatMap { implicit runtime => // This is needed to derive cats-effect instances for that are needed by http4s
    BlazeServerBuilder[RIO[Has[AuthService] with Clock with Blocking, *]]
      .withExecutionContext(runtime.platform.executor.asEC)
      .bindHttp(8081, "0.0.0.0")
      .withHttpApp(Router("/" -> AuthRoutes.routes).orNotFound)
      .serve
      .compile
      .drain
  }

  override def run(args: List[String]): URIO[zio.ZEnv, zio.ExitCode] = {
    serve.provideCustomLayer(service).exitCode
  }
}
