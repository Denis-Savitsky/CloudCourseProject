package ru.hes.auth.service

import cats.effect.IO
import cats.implicits.catsSyntaxApplicativeId
import dev.profunktor.redis4cats.RedisCommands
import ru.hes.auth.api.{AuthRequest, AuthResponse}
import ru.hes.auth.db.DB
import ru.hes.auth.exceptions._
import ru.hes.auth.generator.TokenGenerator

import scala.concurrent.duration.DurationInt

trait AuthService[F[_]] {

  def authorize(request: AuthRequest): F[AuthResponse]

  def signUp(request: AuthRequest): F[AuthResponse]

}

class AuthServiceImpl(redis: RedisCommands[IO, String, String]) extends AuthService[IO] {
  override def authorize(request: AuthRequest): IO[AuthResponse] =
    for {
      _ <- validateCred(request)
      creds <- DB.searchCredentials(request.login)
      _ <- creds match {
        case Some(value) if value.password == request.password => IO.unit
        case None => IO.raiseError(LoginNotFound(request.login))
        case _ => IO.raiseError(InvalidCredentials(request.login))
      }
      token <- TokenGenerator.generateSHAToken()
      _ <- redis.setEx(request.login, token, 1.hours)
    } yield AuthResponse(token)

  override def signUp(request: AuthRequest): IO[AuthResponse] = {
    for {
      _ <- validateCred(request)
      creds <- DB.addUserWithChecking(request.login, request.password)
      _ <- (creds.login == request.login && creds.password == request.password).pure[IO].ifM(IO.unit, IO.raiseError(LoginAlreadyExists(request.login)))
      token <- TokenGenerator.generateSHAToken()
      _ <- redis.setEx(request.login, token, 1.hours)
    } yield AuthResponse(token)
  }

  private def validateCred(body: AuthRequest) = {
    if (body.login.length <= 6) IO.raiseError(InvalidLogin())
    else if (body.password.length <= 6) IO.raiseError(InvalidPassword())
    else IO.unit
  }
}
