package com.example

import com.example.db.Tables
import org.jooq.SQLDialect
import org.jooq.impl.DSL

object Main extends App {

  val e = Tables.EMPLOYEE.as("e")

  val query = DSL.using(SQLDialect.H2)
    .selectFrom(e)
    .limit(100).offset(42)

  println(query)

}
