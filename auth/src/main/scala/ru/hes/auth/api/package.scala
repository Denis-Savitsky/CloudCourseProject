package ru.hes.auth

import derevo.circe.{decoder, encoder}
import derevo.derive

package object api {

  @derive(decoder)
  case class AuthRequest(login: String, password: String)

  @derive(encoder)
  case class AuthResponse(token: String)

}
