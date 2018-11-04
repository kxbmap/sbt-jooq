package com.example

import org.jooq.{Allow, PlainSQL, SQL}

object API {

  @PlainSQL
  def foo: SQL = ???

}

object PlainSQLWartTest {

  @Allow.PlainSQL
  def bar(): Unit = API.foo

}
