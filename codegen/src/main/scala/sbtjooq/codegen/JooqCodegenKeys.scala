package sbtjooq.codegen

import sbt._
import scala.xml.Node

trait JooqCodegenKeys {

  val JooqCodegen = config("jooq-codegen").hide

  val jooqCodegen = taskKey[Seq[File]]("Run jOOQ codegen")

  val jooqCodegenConfig = settingKey[CodegenConfig]("jOOQ codegen configuration")
  val jooqCodegenKeys = settingKey[Seq[CodegenKey]]("jOOQ codegen keys for substitution")
  val jooqCodegenConfigVariables = taskKey[Map[String, String]]("Variables to substitute configuration text")
  val jooqCodegenConfigTransformer = taskKey[Node => Node]("jOOQ codegen configuration transform function")
  val jooqCodegenTransformedConfig = taskKey[Node]("transformed jOOQ codegen configuration")

  val jooqCodegenAutoStrategy = settingKey[AutoStrategy]("jOOQ codegen strategy")

  val jooqCodegenGeneratedSources = taskKey[Seq[File]]("jOOQ codegen generated sources")
  val jooqCodegenGeneratedSourcesFinder = taskKey[PathFinder]("PathFinder for jOOQ codegen generated sources")

}

object JooqCodegenKeys extends JooqCodegenKeys
