package ru.hes.shortener.exceptions

sealed abstract class ShortenerAppException(message: String) extends RuntimeException(message)
case class Unauthorized() extends ShortenerAppException("Unauthorized")
