package ru.hes.shortener.service

import cats.{Monad, MonadError}
import cats.effect.Async
import ru.hes.shortener.model.AuthResponse
import sttp.client3.{SttpBackend, UriContext, basicRequest}
import sttp.client3.asynchttpclient.cats.AsyncHttpClientCatsBackend
import sttp.client3.circe.asJson

trait AuthService[F[_]] {
  def auth(token: String): F[Unit]
}

class AuthServiceImpl[F[_]: Async: MonadError[*[_], Throwable]](backend: SttpBackend[F, Any]) extends AuthService[F] {

  private def request(token: String) = basicRequest
    .header("Authorization", token)
    .get(uri"localhost:8081/auth")
    .response(asJson[AuthResponse])

  override def auth(token: String): F[Unit] = {
    for {
      resp <- backend.send(request(token))
      _ = resp.body match {
        case Left(value) => MonadError[F].raiseError[Unit](new RuntimeException(value))
        case Right(AuthResponse(AuthStatus.Found)) => ().pure[F]
        case Right(AuthResponse(AuthStatus.NotFound)) => MonadError.raiseError[Unit](UnauthorizedException())
      }
    } yield()

  }
}
