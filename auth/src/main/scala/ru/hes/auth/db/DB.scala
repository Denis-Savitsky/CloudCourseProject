package ru.hes.auth.db

import cats.effect.IO

import io.getquill.{PostgresJAsyncContext, SnakeCase}
import zio.{Task, ZIO}
import scala.concurrent.ExecutionContext

trait DB {
  def searchCredentials(login: String): Task[Option[CredentialInfo]]

  def addUserWithChecking(login: String, password: String): Task[CredentialInfo]
}

class DBImpl(override val ctx: PostgresJAsyncContext[SnakeCase.type])(implicit ec: ExecutionContext) extends DB with Queries {

  import ctx._

  override def searchCredentials(login: String): Task[Option[CredentialInfo]] = {
    ZIO.fromFuture { _ =>
      ctx.run(quote {
        (for {
          cred <- creds if cred.login == lift(login)
        } yield cred).take(1)
      }).map(_.headOption)
    }
  }

  override def addUserWithChecking(login: String, password: String): Task[CredentialInfo] =
    for {
      existing <- searchCredentials(login)
      res <- existing match {
        case Some(value) => Task.succeed(value)
        case None => ZIO.fromFuture { _ => ctx.run(quote {
          creds.insert(lift(CredentialInfo(0, login, password))).returningGenerated(r => CredentialInfo(r.id, r.login, r.password))
        })
        }
      }
    } yield res

}

object DBLive {

  val layer = (for {
    implicit0(ec: ExecutionContext) <- ZIO.runtime[Any].map(_.platform.executor.asEC)
    pg = new PostgresJAsyncContext(SnakeCase, "ru.hes.db")
  } yield new DBImpl(pg)).toLayer[DB]
}

