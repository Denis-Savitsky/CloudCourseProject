package ru.hes.shortener.service

import cats.effect.IO
import cats.effect.std.Random
import cats.implicits._

trait KeyGenerator[F[_]] {
  def generateKey(length: Int = 10): F[String]
}

class KeyGeneratorImpl extends KeyGenerator[IO] {

  val letters1: Seq[Char] = ('a' to 'z')
  val letters2: Seq[Char] = ('A' to 'Z')
  val numbers: Seq[Char] = ('0' to '9')

  val symbols: Seq[Char] = letters1 ++ letters2 ++ numbers

  override def generateKey(length: Int): IO[String] = {
    (0 to length).toList.map(_ => generateRandomSymbol()).sequence.map(_.mkString)
  }

  private def generateRandomSymbol(): IO[String] = for {
    random <- Random.scalaUtilRandom[IO]
    key <- random.betweenInt(0, symbols.length)
    s = symbols(key)
  } yield s.toString

}
