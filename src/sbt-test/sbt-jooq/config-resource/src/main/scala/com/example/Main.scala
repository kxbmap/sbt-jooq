package com.example

import com.example.db.Tables
import java.sql.DriverManager
import org.jooq.impl.DSL

object Main extends App {
  Class.forName("org.h2.Driver")

  val conn = DriverManager.getConnection("jdbc:h2:./test")
  try {
    val e = Tables.EMPLOYEE.as("e")

    val query = DSL.using(conn)
      .selectFrom(e)
      .limit(2).offset(1)

    println(s"query:\n${query.getSQL}")
    println(s"params:\n${query.getParams}")

    val result = query.fetch()

    println(s"result:\n$result")
  } finally {
    conn.close()
  }
}
