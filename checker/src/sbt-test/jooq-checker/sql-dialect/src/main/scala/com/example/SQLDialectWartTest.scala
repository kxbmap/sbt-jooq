package com.example

import org.jooq.{SQL, Support};

object API {

  @Support
  def foo: SQL = ???

}

object SQLDialectWartTest {

  def bar(): Unit = API.foo

}
