package sbtjooq.codegen

import sbt._
import scala.xml.{Node, NodeSeq}

trait JooqCodegenKeys {

  val JooqCodegen = config("jooq-codegen").hide

  // Tasks
  val jooqCodegen = taskKey[Seq[File]]("Run jOOQ-codegen")
  val jooqCodegenIfAbsent = taskKey[Seq[File]]("Run jOOQ-codegen if generated files absent")

  // User settings
  val jooqCodegenMode = settingKey[CodegenMode]("jOOQ-codegen execution mode")
  val jooqCodegenConfig = settingKey[CodegenConfig]("jOOQ-codegen configuration")
  val jooqCodegenVariables = settingKey[Map[String, Any]]("Variables to replace configuration placeholders")

  // Customizable
  val jooqSource = settingKey[File]("Default generated jOOQ source directory")
  val jooqCodegenConfigTransformer = settingKey[Node => Node]("jOOQ-codegen configuration transform function")
  val jooqCodegenVariableExpander = settingKey[PartialFunction[Any, NodeSeq]]("Partial function to expand variables")

  // Internal use
  val jooqCodegenTransformedConfigs = taskKey[Seq[Node]]("Transformed jOOQ-codegen configurations")
  val jooqCodegenConfigFiles = taskKey[Seq[File]]("Actual jOOQ-codegen configuration files")
  val jooqCodegenGeneratedSources = taskKey[Seq[File]]("jOOQ-codegen generated sources")
  val jooqCodegenGeneratorTargets = taskKey[Seq[(File, File)]]("jOOQ-codegen generator target directory")
  val jooqCodegenGeneratedSourcesFinders = taskKey[Seq[(File, PathFinder)]]("PathFinders for generated sources")

}

object JooqCodegenKeys extends JooqCodegenKeys
