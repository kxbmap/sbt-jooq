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

package sbtjooq.checker

import sbt._
import sbt.Keys._
import sbtjooq.JooqPlugin
import sbtjooq.checker.JooqCheckerKeys._
import sbtjooq.checker.internal.JooqWarts
import wartremover.WartRemover
import wartremover.WartRemover.autoImport._

object JooqCheckerPlugin extends AutoPlugin {

  override def requires: Plugins = JooqPlugin && WartRemover

  object autoImport extends JooqCheckerKeys {

    final val CheckerLevel = sbtjooq.checker.CheckerLevel

  }

  override def globalSettings: Seq[Setting[_]] = Seq(
    jooqCheckerLevels := CheckerLevels.default,
    jooqCheckerJooqWartsVersion := BuildInfo.defaultJooqWartsVersion,
  )

  override def projectConfigurations: Seq[Configuration] = Seq(JooqChecker)

  override def projectSettings: Seq[Setting[_]] =
    jooqCheckerDefaultSettings ++
      inConfig(Compile)(inTask(compile)(jooqCheckerSettings))

  private def jooqCheckerDefaultSettings: Seq[Setting[_]] = Seq(
    libraryDependencies +=
      ("com.github.kxbmap" %% "jooq-warts" % jooqCheckerJooqWartsVersion.value % JooqChecker).intransitive()
  ) ++
    JooqPlugin.jooqDependencies(JooqChecker) ++
    inConfig(JooqChecker)(
      Seq(
        managedClasspath := Classpaths.managedJars(JooqChecker, classpathTypes.value, update.value)
      )
    )

  lazy val jooqCheckerSettings: Seq[Setting[_]] = Seq(
    wartremoverClasspaths ++= (JooqChecker / managedClasspath).value.files.map(_.toURI.toString),
    wartremoverErrors := wartremoverErrors.value.filterNot(JooqWarts.all) ++ jooqCheckerLevels.value.errors,
    wartremoverWarnings := wartremoverWarnings.value.filterNot(JooqWarts.all) ++ jooqCheckerLevels.value.warnings,
  )

}
