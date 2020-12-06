package sbtjooq.checker.internal

import wartremover.Wart

object JooqWarts {

  val PlainSQL: Wart = Wart.custom("jooqwarts.PlainSQL")

  val SQLDialect: Wart = Wart.custom("jooqwarts.SQLDialect")

  val all: Set[Wart] = Set(PlainSQL, SQLDialect)

}
