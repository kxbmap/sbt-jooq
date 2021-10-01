package sbtjooq.codegen

import sbt._
import scala.xml.Node

trait JooqCodegenKeys {

  val JooqCodegen = config("jooq-codegen").hide

  // Tasks
  val jooqCodegen = taskKey[Seq[File]]("Run jOOQ-codegen")
  val jooqCodegenIfAbsent = taskKey[Seq[File]]("Run jOOQ-codegen if generated files absent")

  // User settings
  val jooqCodegenConfig = settingKey[CodegenConfig]("jOOQ-codegen configuration")
  val jooqCodegenVariables = settingKey[Map[String, String]]("Variables to replace configuration text")
  val jooqCodegenAutoStrategy = settingKey[AutoStrategy]("jOOQ-codegen auto generation strategy")

  // For reference
  val jooqCodegenConfigFiles = taskKey[Seq[File]]("Actual jOOQ-codegen configuration files")
  val jooqCodegenGeneratedSources = taskKey[Seq[File]]("jOOQ-codegen generated sources")

}

object JooqCodegenKeys extends JooqCodegenKeys


trait JooqCodegenInternalKeys {

  // For internal use
  val jooqCodegenConfigTransformer = settingKey[Node => Node]("jOOQ-codegen configuration transform function")
  val jooqCodegenTransformedConfigs = taskKey[Seq[Node]]("Transformed jOOQ-codegen configurations")
  val jooqCodegenGeneratorTargets = taskKey[Seq[(File, File)]]("jOOQ-codegen generator target directory")
  val jooqCodegenGeneratedSourcesFinders = taskKey[Seq[(File, PathFinder)]]("PathFinders for generated sources")

}

object JooqCodegenInternalKeys extends JooqCodegenInternalKeys
