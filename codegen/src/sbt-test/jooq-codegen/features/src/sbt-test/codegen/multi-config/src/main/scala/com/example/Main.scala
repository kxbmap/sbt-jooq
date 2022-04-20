package com.example

import com.example.db0.{Tables => T0}
import com.example.db1.{Tables => T1}
import java.sql.DriverManager
import org.jooq.impl.DSL
import scala.util.Using

object Main extends App {
  Class.forName("org.h2.Driver")

  Using.resources(
    DriverManager.getConnection(sys.props("scripted.jdbc.url.0")),
    DriverManager.getConnection(sys.props("scripted.jdbc.url.1")),
  ) { (conn0, conn1) =>
    val q0 = DSL.using(conn0).selectFrom(T0.EMPLOYEE.as("e"))
    println(q0.getSQL)
    println(q0.fetch())

    val q1 = DSL.using(conn1).selectFrom(T1.COMPANY.as("c"))
    println(q1.getSQL)
    println(q1.fetch())
  }

}
