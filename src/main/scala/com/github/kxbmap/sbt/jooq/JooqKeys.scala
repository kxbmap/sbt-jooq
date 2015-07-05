package com.github.kxbmap.sbt.jooq

import sbt._
import scala.xml.transform.RewriteRule

object JooqKeys {

  val jooq = config("jooq").hide

  val jooqVersion = settingKey[String]("jOOQ version")

  val jooqCodegen = taskKey[Seq[File]]("Run jOOQ codegen")
  val jooqCodegenConfigFile = settingKey[File]("jOOQ codegen configuration file")
  val jooqCodegenConfigRewriteRules = settingKey[Seq[RewriteRule]]("jOOQ codegen configuration rewrite rules")
  val jooqCodegenConfig = taskKey[xml.Node]("jOOQ codegen configuration")

}
