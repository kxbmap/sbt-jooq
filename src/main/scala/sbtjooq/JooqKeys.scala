package sbtjooq

import sbt._

trait JooqKeys {

  val jooqVersion = settingKey[String]("jOOQ version")
  val jooqOrganization = settingKey[String]("jOOQ organization/group ID")
  val autoJooqLibrary = settingKey[Boolean]("Add jOOQ dependencies if true")

}

object JooqKeys extends JooqKeys
