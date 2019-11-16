package sbtjooq.checker.internal

import wartremover.Wart

object JooqWarts {

  val DefaultVersion = "0.1.2"

  val PlainSQL: Wart = Wart.custom("jooqwarts.PlainSQL")

  val SQLDialect: Wart = Wart.custom("jooqwarts.SQLDialect")

  val all: Set[Wart] = Set(PlainSQL, SQLDialect)

}
