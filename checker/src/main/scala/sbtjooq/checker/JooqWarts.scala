package sbtjooq.checker

import wartremover.Wart

private[sbtjooq] object JooqWarts {

  val DefaultVersion = "0.1.1"

  val PlainSQL: Wart = Wart.custom("jooqwarts.PlainSQL")

  val SQLDialect: Wart = Wart.custom("jooqwarts.SQLDialect")

}
