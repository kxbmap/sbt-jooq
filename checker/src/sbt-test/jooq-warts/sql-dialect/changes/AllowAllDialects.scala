package com.example

import org.jooq.{Allow, SQL, Support};

object API {

  @Support
  def foo: SQL = ???

}

object SQLDialectWartTest {

  @Allow
  def bar(): Unit = API.foo

}
