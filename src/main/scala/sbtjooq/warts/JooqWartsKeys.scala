package sbtjooq.warts

import sbt._

trait JooqWartsKeys {

  val JooqWarts = config("jooq-warts").hide

  val jooqWartsVersion = settingKey[String]("jooq-warts version")

}

object JooqWartsKeys extends JooqWartsKeys
