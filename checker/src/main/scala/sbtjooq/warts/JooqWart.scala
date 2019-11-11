package sbtjooq.warts

import wartremover.Wart

object JooqWart {

  val PlainSQL = Wart.custom("jooqwarts.PlainSQL")

  val SQLDialect = Wart.custom("jooqwarts.SQLDialect")

}
