package ru.hes.auth.api

import cats.effect.IO
import cats.syntax.all._
import io.circe.generic.auto.{exportDecoder, exportEncoder}
import org.http4s.HttpRoutes
import ru.hes.auth.exceptions._
import ru.hes.auth.service.AuthService
import sttp.model.StatusCode
import sttp.tapir._
import sttp.tapir.docs.openapi.OpenAPIDocsInterpreter
import sttp.tapir.generic.auto.schemaForCaseClass
import sttp.tapir.json.circe._
import sttp.tapir.openapi.OpenAPI
import sttp.tapir.openapi.circe.yaml.RichOpenAPI
import sttp.tapir.server.http4s.Http4sServerInterpreter
import sttp.tapir.swagger.SwaggerUI

class AuthRoutes(authService: AuthService[IO]) {

  val basicEndpoint =
    endpoint
    .errorOut(
      oneOf[AuthException](
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
      .serverLogicRecoverErrors(body => authService.authorize(body))

  val register =
    basicEndpoint
      .post
      .in("register")
      .in(jsonBody[AuthRequest])
      .out(jsonBody[AuthResponse])
      .serverLogicRecoverErrors(body => authService.signUp(body))

  val check =
    endpoint
      .get
      .in("auth")
      .in(header[String]("Authorization"))
      .out(stringBody)
      .serverLogicSuccess(_ => IO{"access"})



  val apiRoutes: HttpRoutes[IO] =
    Http4sServerInterpreter[IO]().toRoutes(List(auth, register, check))

  val openApiDocs: OpenAPI = OpenAPIDocsInterpreter().toOpenAPI(List(auth, register).map(_.endpoint), "Auth service", "0.1.0")
  val openApiYml: String = openApiDocs.toYaml

  val swaggerUIRoutes: HttpRoutes[IO] = Http4sServerInterpreter[IO]().toRoutes(SwaggerUI[IO](openApiYml))

  val routes: HttpRoutes[IO] = apiRoutes <+> swaggerUIRoutes

}
