package com.example

import java.sql.DriverManager
import org.jooq.impl.DSL

object Main extends App {
  Class.forName("org.h2.Driver")

  val conn1 = DriverManager.getConnection("jdbc:h2:./db1")
  val conn2 = DriverManager.getConnection("jdbc:h2:./db2")
  try {
    val employees = DSL.using(conn1).selectFrom(db1.Tables.EMPLOYEE).fetch()
    val companies = DSL.using(conn2).selectFrom(db2.Tables.COMPANY).fetch()

    println(s"Employees from db1:\n$employees")
    println(s"Companies from db2:\n$companies")
  } finally {
    conn1.close()
    conn2.close()
  }
}
