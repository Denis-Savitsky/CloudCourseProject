package ru.hes.auth

import derevo.circe.{decoder, encoder}
import derevo.derive
import enumeratum.{CirceEnum, Enum, EnumEntry}

package object api {

  @derive(decoder)
  case class AuthRequest(login: String, password: String)

  @derive(encoder)
  case class AuthResponse(token: String)

  @derive(encoder)
  case class TokenSearchResponse(status: AuthStatus)


  sealed trait AuthStatus extends EnumEntry

  object AuthStatus extends CirceEnum[AuthStatus] with Enum[AuthStatus] {
    case object NotFound extends AuthStatus

    case object Found extends AuthStatus

    override def values: IndexedSeq[AuthStatus] = findValues
  }

}
