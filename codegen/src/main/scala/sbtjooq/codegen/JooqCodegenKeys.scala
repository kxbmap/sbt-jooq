package sbtjooq.codegen

import sbt._
import scala.xml.Node

trait JooqCodegenKeys {

  val JooqCodegen = config("jooq-codegen").hide

  val jooqCodegen = taskKey[Seq[File]]("Run jOOQ-codegen")

  val jooqCodegenConfig = settingKey[CodegenConfig]("jOOQ-codegen configuration")
  val jooqCodegenVariables = settingKey[Map[String, String]]("Variables to replace configuration text")
  val jooqCodegenAutoStrategy = settingKey[AutoStrategy]("jOOQ-codegen strategy")

  val jooqCodegenConfigTransformer = settingKey[Node => Node]("jOOQ-codegen configuration transform function")
  val jooqCodegenTransformedConfig = taskKey[Node]("Transformed jOOQ-codegen configuration XML")
  val jooqCodegenTransformedConfigFile = taskKey[File]("Transformed jOOQ-codegen configuration file")
  val jooqCodegenGeneratorTarget = taskKey[File]("jOOQ-codegen generator target directory")
  val jooqCodegenGeneratedSources = taskKey[Seq[File]]("jOOQ-codegen generated sources")
  val jooqCodegenGeneratedSourcesFinder = taskKey[PathFinder]("PathFinder for generated sources")

}

object JooqCodegenKeys extends JooqCodegenKeys
