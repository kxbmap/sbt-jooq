package com.example

import com.example.db.Tables._
import java.sql.DriverManager
import org.jooq.impl.DSL
import scala.util.Using

object Main extends App {
  Class.forName("org.h2.Driver")

  Using.resource(DriverManager.getConnection("jdbc:h2:./test")) { conn =>
    val query = DSL.using(conn).selectFrom(EMPLOYEE.as("e"))
    println(query.getSQL)
    println(query.fetch())
  }
}
