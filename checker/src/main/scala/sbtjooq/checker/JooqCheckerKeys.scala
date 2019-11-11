package sbtjooq.checker

import sbt._

trait JooqCheckerKeys {

  val JooqChecker = config("jooq-checker").hide

  val jooqCheckerJooqWartsVersion = settingKey[String]("kxbmap/jooq-warts version")

}

object JooqCheckerKeys extends JooqCheckerKeys
