package ru.hes.auth

package object db {
  case class CredentialInfo(id: Long, login: String, password: String)
}
