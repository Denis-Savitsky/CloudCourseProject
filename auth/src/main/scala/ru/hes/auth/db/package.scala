package ru.hes.auth.db

import io.getquill.{PostgresJAsyncContext, SnakeCase}

trait Queries {

  protected val ctx :PostgresJAsyncContext[SnakeCase.type]
  import ctx._

  val creds = quote {
    querySchema[CredentialInfo]("public.creds")
  }

}
