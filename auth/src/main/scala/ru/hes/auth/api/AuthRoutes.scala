package ru.hes.auth.api

import cats.effect.IO
import cats.syntax.all._
import io.circe.generic.auto.{exportDecoder, exportEncoder}
import org.http4s.HttpRoutes
import ru.hes.auth.exceptions._
import ru.hes.auth.service.AuthService
import sttp.model.StatusCode
import sttp.tapir.ztapir._
import sttp.tapir.docs.openapi.OpenAPIDocsInterpreter
import sttp.tapir.generic.auto.schemaForCaseClass
import sttp.tapir.json.circe._
import sttp.tapir.openapi.OpenAPI
import sttp.tapir.openapi.circe.yaml.RichOpenAPI
import sttp.tapir.server.http4s.Http4sServerInterpreter
import sttp.tapir.server.http4s.ztapir.ZHttp4sServerInterpreter
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import sttp.tapir.swagger.SwaggerUI
import zio.clock.Clock
import zio.blocking.Blocking
import zio._
import zio.interop.catz._

object AuthRoutes {

  val basicEndpoint =
    endpoint
    .errorOut(
      oneOf[Exception](
        oneOfMappingFromMatchType(StatusCode.BadRequest, jsonBody[InvalidLogin].description("invalid login")),
        oneOfMappingFromMatchType(StatusCode.BadRequest, jsonBody[InvalidPassword].description("invalid password")),
        oneOfMappingFromMatchType(StatusCode.Unauthorized, jsonBody[LoginNotFound].description("login not found")),
        oneOfMappingFromMatchType(StatusCode.Unauthorized, jsonBody[InvalidCredentials].description("wrong password")),
        oneOfMappingFromMatchType(StatusCode.Conflict, jsonBody[LoginAlreadyExists].description("login already occupied"))

      )
    )

  val auth =
    basicEndpoint
      .post
      .in("auth")
      .in(jsonBody[AuthRequest])
      .out(jsonBody[AuthResponse])
      .serverLogicRecoverErrors(body => AuthService.authorize(body))

  val register =
    basicEndpoint
      .post
      .in("register")
      .in(jsonBody[AuthRequest])
      .out(jsonBody[AuthResponse])
      .serverLogicRecoverErrors(body => AuthService.signUp(body))

  val check =
    basicEndpoint
      .get
      .in("auth")
      .in(header[String]("Authorization"))
      .out(jsonBody[TokenSearchResponse])
      .serverLogicSuccess(token => AuthService.findToken(token))

  val apiRoutes: HttpRoutes[RIO[Has[AuthService] with Clock with Blocking, *]] =
    ZHttp4sServerInterpreter().from(List(auth, register, check)).toRoutes

  val swaggerUIRoutes: HttpRoutes[RIO[Has[AuthService] with Clock with Blocking, *]] =
    ZHttp4sServerInterpreter()
      .from(SwaggerInterpreter().fromEndpoints[RIO[Has[AuthService] with Clock with Blocking, *]](List(auth, register, check).map(_.endpoint), "Auth service", "0.1.0"))
      .toRoutes

  val routes:  HttpRoutes[RIO[Has[AuthService] with Clock with Blocking, *]] = apiRoutes <+> swaggerUIRoutes

}
