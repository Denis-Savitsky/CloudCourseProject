package ru.hes.auth

import cats.effect.{ExitCode, IO, IOApp, Resource}
import dev.profunktor.redis4cats.Redis
import dev.profunktor.redis4cats.effect.Log.Stdout.instance
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.server.Router
import ru.hes.auth.api.AuthRoutes
import ru.hes.auth.service.AuthServiceImpl

object Main extends IOApp.Simple {


  lazy val service = Redis[IO].utf8("redis://default:IXI2wGgDPq6CnJx6VJdmaWtERV0ay2Mh@redis-10438.c53.west-us.azure.cloud.redislabs.com:10438").map(redis => new AuthRoutes(new AuthServiceImpl(redis)))
//  val service1 = Redis[IO].utf8("rediss://redis-15478.c55.eu-central-1-1.ec2.cloud.redislabs.com:15478").use(redis => redis.auth("kwRVtQwACIUsDKIvvzovDt041GrQqyaT") *> IO {new AuthRoutes(new AuthServiceImpl(redis))})

//  val service = Redis[IO].utf8("").use {
//    redis => IO {new AuthRoutes(new AuthServiceImpl(redis))}
//  }


//  override def run: IO[Unit] =
//    for {
//      router <- service1
//      a <- BlazeServerBuilder[IO]
//              .bindHttp(8080, "localhost")
//              .withHttpApp(Router("/" -> router.routes).orNotFound)
//              .resource
//              .use { _ =>
//                IO {println("Go to l")}
//              }
//              .as(ExitCode.Success)
//    } yield a


  override def run: IO[Unit] = {
    service.use { router =>
      BlazeServerBuilder[IO]
        .bindHttp(8080, "0.0.0.0")
        .withHttpApp(Router("/" -> router.routes).orNotFound)
        .resource
        .useForever
        .as(ExitCode.Success)
    }
  }
}
