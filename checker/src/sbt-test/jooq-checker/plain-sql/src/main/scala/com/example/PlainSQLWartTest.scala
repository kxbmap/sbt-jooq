package com.example

import org.jooq.{PlainSQL, SQL}

object API {

  @PlainSQL
  def foo: SQL = ???

}

class PlainSQLWartTest {

  def bar(): Unit = API.foo

}
