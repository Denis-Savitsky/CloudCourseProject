package ru.hes.auth.db

import cats.effect.IO
import doobie.free.connection
import doobie.implicits.toSqlInterpolator
import doobie.util.transactor.Transactor
import doobie.syntax.connectionio._

object DB {

  lazy val xa = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver",
    "",
    "user",
    "pass"
  )

  def searchCredentials(login: String) =
    sql"""select * from public.creds c
         where c = $login
       """.query[CredentialInfo].option.transact(xa)

  def addUserWithChecking(login: String, password: String) =
    (for {
      existing <- sql"""select * from public.creds c where c = $login""".query[CredentialInfo].option
      res <- existing match {
        case Some(value) => connection.pure(value)
        case None => sql"insert into public.creds (login, password) values ($login, $password)"
          .update
          .withUniqueGeneratedKeys[CredentialInfo]("id", "login", "password")
      }
    } yield res).transact(xa)

}

