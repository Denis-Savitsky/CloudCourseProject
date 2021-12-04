package ru.hes.shortener.model

import derevo.circe.decoder
import derevo.derive

@derive(decoder)
case class AuthResponse(status: AuthStatus)
