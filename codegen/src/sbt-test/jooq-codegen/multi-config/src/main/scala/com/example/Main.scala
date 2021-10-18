package com.example

import com.example.db1.{Tables => T1}
import com.example.db2.{Tables => T2}
import java.sql.DriverManager
import org.jooq.impl.DSL
import scala.util.Using

object Main extends App {
  Class.forName("org.h2.Driver")

  Using.resources(
    DriverManager.getConnection("jdbc:h2:./db1"),
    DriverManager.getConnection("jdbc:h2:./db2"),
  ) { (conn1, conn2) =>
    val q1 = DSL.using(conn1).selectFrom(T1.EMPLOYEE)
    val r1 = q1.fetch()
    println(s"Result of db1:\n$r1")

    val q2 = DSL.using(conn2).selectFrom(T2.COMPANY)
    val r2 = q2.fetch()
    println(s"Result of db2:\n$r2")
  }

}
