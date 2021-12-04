package ru.hes.auth

package object exceptions {

  sealed abstract class AuthException(val message: String) extends RuntimeException(message)
  final case class InvalidLogin() extends AuthException("invalid login")
  final case class InvalidPassword() extends AuthException("invalid password")
  final case class LoginNotFound(login: String) extends AuthException(s"$login not found")
  final case class InvalidCredentials(login: String) extends AuthException(s"password for$login is invalid")
  final case class LoginAlreadyExists(login: String) extends AuthException(s"login $login already exists")

}
