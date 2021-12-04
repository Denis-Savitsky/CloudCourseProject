package ru.hes.shortener.api

import cats.effect._
import org.http4s._
import org.http4s.circe.CirceEntityCodec._
import org.http4s.dsl.io._
import ru.hes.shortener.service._


class ShortenerRoutes(
   service: ShortenerService[IO]
) {

  val routes = HttpRoutes.of[IO] {
    case req @ POST -> Root / "shorten" =>
      for {
        body <- req.as[ShortenerRequest]
        shortLink <- service.createShortLink(body)
        resp <-Ok(shortLink)
      } yield resp
    case GET -> Root / short =>
      for {
        link <-  service.getRedirectLink(GetLinkRequest(short))
        resp <- PermanentRedirect("","Location" -> link)
        _ = println(resp.headers)
      } yield resp
  }

}
