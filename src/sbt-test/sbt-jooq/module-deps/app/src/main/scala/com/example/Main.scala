package com.example

import com.example.db.Tables
import java.sql.DriverManager
import org.jooq.Record
import org.jooq.impl.DSL

object Main extends App {
  Class.forName("org.h2.Driver")

  val conn = DriverManager.getConnection("jdbc:h2:./test")
  try {
    val dsl = DSL.using(conn)
    val t = Tables.TEST.as("t")
    val q = dsl.selectFrom(t).where(t.ID.eq(1))

    println(s"query : ${q.getSQL}")
    println(s"params: ${q.getParams}")

    val r = q.fetchOne()

    println(s"result:\n$r")
  } finally {
    conn.close()
  }
}
