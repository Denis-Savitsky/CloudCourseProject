package ru.hes.shortener.api

import cats.effect._
import org.http4s._
import org.http4s.circe.CirceEntityCodec._
import org.http4s.dsl.io._
import ru.hes.shortener.exceptions.UnauthorizedException
import ru.hes.shortener.service._


class ShortenerRoutes(
   service: ShortenerService[IO],
   authService: AuthService[IO]
) {

  val routes = HttpRoutes.of[IO] {
    case req @ POST -> Root / "shorten" =>
      for {
        _ <- req.headers.headers.find(_.name.toString == "Authorization") match {
          case Some(token) => authService.auth(token.value)
          case None => IO.raiseError(UnauthorizedException())
        }
        body <- req.as[ShortenerRequest]
        shortLink <- service.createShortLink(body)
        resp <-Ok(shortLink)
      } yield resp
    case req @ GET -> Root / short =>
      for {
        _ <- req.headers.headers.find(_.name.toString == "Authorization") match {
          case Some(token) => authService.auth(token.value)
          case None => IO.raiseError(UnauthorizedException())
        }
        link <-  service.getRedirectLink(GetLinkRequest(short))
        resp <- PermanentRedirect("","Location" -> link)
        _ = println(resp.headers)
      } yield resp
  }

}
