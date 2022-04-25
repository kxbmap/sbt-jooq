package com.example

import com.example.db0.{Tables => T0}
import com.example.db1.{Tables => T1}
import org.jooq.impl.DSL
import scala.util.Using

object Main extends App {
  Class.forName("org.h2.Driver")

  Using.resources(
    DSL.using(sys.props("scripted.jdbc.url.0")),
    DSL.using(sys.props("scripted.jdbc.url.1")),
  ) { (create0, create1) =>
    val q0 = create0.selectFrom(T0.EMPLOYEE.as("e"))
    println(q0.getSQL)
    println(q0.fetch())

    val q1 = create1.selectFrom(T1.COMPANY.as("c"))
    println(q1.getSQL)
    println(q1.fetch())
  }

}
