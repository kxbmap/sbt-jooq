/*
 * Copyright 2015 Tsukasa Kitachi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
  val jooqCodegenTransformedConfigFiles = taskKey[Seq[File]]("Actual jOOQ-codegen configuration files")
  val jooqCodegenGeneratedSources = taskKey[Seq[File]]("jOOQ-codegen generated sources")
  val jooqCodegenGeneratorTargets = taskKey[Seq[(File, File)]]("jOOQ-codegen generator target directory")
  val jooqCodegenGeneratedSourcesFinders = taskKey[Seq[(File, PathFinder)]]("PathFinders for generated sources")

}

object JooqCodegenKeys extends JooqCodegenKeys
