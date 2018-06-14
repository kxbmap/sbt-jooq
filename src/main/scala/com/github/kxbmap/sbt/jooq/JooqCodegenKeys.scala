package com.github.kxbmap.sbt.jooq

import sbt._
import scala.xml.Node

trait JooqCodegenKeys {

  val Jooq = config("jooq").hide

  val jooqVersion = settingKey[String]("jOOQ version")
  val jooqOrganization = settingKey[String]("jOOQ organization/group ID")
  val autoJooqLibrary = settingKey[Boolean]("Add jOOQ dependencies if true")

  val jooqCodegen = taskKey[Seq[File]]("Run jOOQ codegen")
  val jooqCodegenConfig = settingKey[CodegenConfig]("jOOQ codegen configuration")
  val jooqCodegenConfigSubstitutions = settingKey[Map[String, String]]("jOOQ codegen configuration text substitutions")
  val jooqCodegenConfigTransformer = settingKey[Node => Node]("jOOQ codegen configuration transform function")
  val jooqCodegenTransformedConfig = taskKey[Node]("transformed jOOQ codegen configuration")
  val jooqCodegenStrategy = settingKey[CodegenStrategy]("jOOQ codegen strategy")
  val jooqCodegenGeneratedSourcesFinder = taskKey[PathFinder]("PathFinder for jOOQ codegen generated sources")

  @deprecated("Use jooqOrganization", "0.4.0")
  val jooqGroupId = jooqOrganization

}

object JooqCodegenKeys extends JooqCodegenKeys
