package ru.hes.shortener.service

import cats.effect.IO
import dev.profunktor.redis4cats.RedisCommands
import ru.hes.shortener.api._

import scala.concurrent.duration.DurationInt

trait ShortenerService[F[_]] {
  def createShortLink(shortenerRequest: ShortenerRequest): F[ShortenerResponse]
  def getRedirectLink(request: GetLinkRequest): F[String]
}

class ShortenerServiceImpl(generator: KeyGenerator[IO], redis: RedisCommands[IO, String, String]) extends ShortenerService[IO] {
  override def createShortLink(shortenerRequest: ShortenerRequest): IO[ShortenerResponse] = for {
    key <- generator.generateKey()
    _ <- redis.setEx(key, shortenerRequest.link, 24.hours)
  } yield ShortenerResponse(key)

  override def getRedirectLink(request: GetLinkRequest): IO[String] =
    for {
      value <- redis.get(request.shortLink)
      link <- value.fold(
        IO.raiseError[String](new RuntimeException)
      )(IO.pure)
  } yield link

}


