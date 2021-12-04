package ru.hes.shortener.model

import enumeratum._

sealed trait AuthStatus extends EnumEntry

object AuthStatus extends CirceEnum[AuthStatus] with Enum[AuthStatus] {
  case object NotFound extends AuthStatus

  case object Found extends AuthStatus

  override def values: IndexedSeq[AuthStatus] = findValues
}
