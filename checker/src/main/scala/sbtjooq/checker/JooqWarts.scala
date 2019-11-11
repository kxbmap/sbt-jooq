package sbtjooq.checker

import wartremover.Wart

private[checker] object JooqWarts {

  val DefaultVersion = "0.1.1"

  val PlainSQL: Wart = Wart.custom("jooqwarts.PlainSQL")

  val SQLDialect: Wart = Wart.custom("jooqwarts.SQLDialect")

}
