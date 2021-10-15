package com.example

import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.jooq.impl.DSL._

object Main extends App {

  val e = table("EMPLOYEE").as("e")

  val query = DSL.using(SQLDialect.H2).selectFrom(e)

  println(query)

}
