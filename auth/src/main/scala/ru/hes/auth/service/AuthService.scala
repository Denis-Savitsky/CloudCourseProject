package ru.hes.auth.service

import mouse.boolean._
import dev.profunktor.redis4cats.RedisCommands
import ru.hes.auth.api.{AuthRequest, AuthResponse, AuthStatus, TokenSearchResponse}
import ru.hes.auth.db.DB
import ru.hes.auth.exceptions._
import ru.hes.auth.generator.TokenGenerator
import zio.{Has, IO, Task, URLayer, ZIO}

import scala.concurrent.duration.DurationInt

trait AuthService {

  def authorize(request: AuthRequest): Task[AuthResponse]

  def signUp(request: AuthRequest): Task[AuthResponse]

  def findToken(token: String): Task[TokenSearchResponse]

}

class AuthServiceImpl(redis: RedisCommands[Task, String, String], db: DB, tokenGenerator: TokenGenerator) extends AuthService {

  override def authorize(request: AuthRequest): Task[AuthResponse] =
    for {
      _ <- validateCred(request)
      creds <- db.searchCredentials(request.login)
      _ <- creds match {
        case Some(value) if value.password == request.password => ZIO.unit
        case None => ZIO.fail(LoginNotFound(request.login))
        case _ => ZIO.fail(InvalidCredentials(request.login))
      }
      token <- tokenGenerator.generateSHAToken(request.login)
      _ <- redis.setEx(token, token, 1.hours)
    } yield AuthResponse(token)

  override def signUp(request: AuthRequest): Task[AuthResponse] = {
    for {
      _ <- validateCred(request)
      creds <- db.addUserWithChecking(request.login, request.password)
      _ <- (creds.login == request.login && creds.password == request.password).fold(ZIO.unit, ZIO.fail(LoginAlreadyExists(request.login)))
      token <- tokenGenerator.generateSHAToken(request.login)
      _ <- redis.setEx(request.login, token, 1.hours)
    } yield AuthResponse(token)
  }


  private def validateCred(body: AuthRequest): IO[AuthException, Unit] = {
    if (body.login.length <= 4) ZIO.fail(InvalidLogin())
    else if (body.password.length <= 4) ZIO.fail(InvalidPassword())
    else ZIO.unit
  }

  override def findToken(token: String): Task[TokenSearchResponse] = {
    for {
      res <- redis.get(token.split("-").headOption.getOrElse(""))
      status = if (res.contains(token)) AuthStatus.Found else AuthStatus.NotFound
    } yield TokenSearchResponse(status)
  }
}

object AuthServiceLive {
  val layer: URLayer[Has[RedisCommands[Task, String, String]] with Has[DB] with Has[TokenGenerator], Has[AuthService]] =
    (new AuthServiceImpl(_, _, _)).toLayer[AuthService]
}

object AuthService {
  def authorize(request: AuthRequest) =
    ZIO.serviceWith[AuthService](_.authorize(request))

  def signUp(request: AuthRequest) =
    ZIO.serviceWith[AuthService](_.signUp(request))

  def findToken(token: String) =
    ZIO.serviceWith[AuthService](_.findToken(token))
}


