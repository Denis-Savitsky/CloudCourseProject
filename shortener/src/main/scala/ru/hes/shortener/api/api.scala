package ru.hes.shortener

import derevo.circe.{decoder, encoder}
import derevo.derive

package object api {

  @derive(decoder)
  case class ShortenerRequest(link: String)

  @derive(decoder)
  case class GetLinkRequest(shortLink: String)

  @derive(encoder)
  case class ShortenerResponse(link: String)

  case class GetLinkResponse(shortLink: String)
}
