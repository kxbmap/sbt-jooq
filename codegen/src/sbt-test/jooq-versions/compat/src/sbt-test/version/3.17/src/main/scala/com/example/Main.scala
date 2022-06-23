package com.example

import com.example.db.Tables._
import org.jooq.impl.DSL
import scala.util.Using

object Main extends App {
  Using.resource(DSL.using(sys.props("scripted.jdbc.url"))) { create =>
    val query = create.selectFrom(EMPLOYEE.as("e"))
    println(query.getSQL)
    println(query.fetch())
  }
}
