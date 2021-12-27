package ru.hes.shortener.service

import cats.implicits.catsSyntaxApplicativeId
import cats.syntax.functor.toFunctorOps
import cats.{Functor, MonadError}
import ru.hes.shortener.exceptions.UnauthorizedException
import ru.hes.shortener.model.{AuthResponse, AuthStatus}
import sttp.client3.circe.asJson
import sttp.client3.{SttpBackend, UriContext, basicRequest}

trait AuthService[F[_]] {
  def auth(token: String): F[Unit]
}

class AuthServiceImpl[F[_] : MonadError[*[_], Throwable]: Functor](backend: SttpBackend[F, Any]) extends AuthService[F] {

  private def request(token: String) = basicRequest
    .header("Authorization", token)
    .get(uri"http://127.0.0.1:8081/auth")
    .response(asJson[AuthResponse])

  override def auth(token: String): F[Unit] = {
    for {
      resp <- backend.send(request(token))
      _ = resp.body match {
        case Left(error) => MonadError[F, Throwable].raiseError[Unit](error)
        case Right(AuthResponse(AuthStatus.Found)) => ().pure[F]
        case Right(AuthResponse(AuthStatus.NotFound)) => MonadError[F, Throwable].raiseError[Unit](UnauthorizedException())
      }
    } yield ()
  }
}
