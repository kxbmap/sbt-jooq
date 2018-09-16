package sbtjooq.codegen

import sbt._
import scala.xml.Node

trait JooqCodegenKeys {

  val Jooq = config("jooq").hide

  val jooqVersion = settingKey[String]("jOOQ version")
  val jooqOrganization = settingKey[String]("jOOQ organization/group ID")
  val autoJooqLibrary = settingKey[Boolean]("Add jOOQ dependencies if true")

  val jooqCodegen = taskKey[Seq[File]]("Run jOOQ codegen")
  val jooqCodegenConfig = settingKey[CodegenConfig]("jOOQ codegen configuration")
  val jooqCodegenKeys = settingKey[Seq[CodegenKey]]("jOOQ codegen keys for substitution")
  val jooqCodegenSubstitutions = taskKey[Seq[(String, String)]]("jOOQ codegen configuration substitution values")
  val jooqCodegenConfigTransformer = taskKey[Node => Node]("jOOQ codegen configuration transform function")
  val jooqCodegenTransformedConfig = taskKey[Node]("transformed jOOQ codegen configuration")
  val jooqCodegenStrategy = settingKey[CodegenStrategy]("jOOQ codegen strategy")
  val jooqCodegenGeneratedSources = taskKey[Seq[File]]("jOOQ codegen generated sources")
  val jooqCodegenGeneratedSourcesFinder = taskKey[PathFinder]("PathFinder for jOOQ codegen generated sources")

}

object JooqCodegenKeys extends JooqCodegenKeys
