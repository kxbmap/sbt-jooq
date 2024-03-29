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

import sbt.*
import sbt.Keys.*
import sbtjooq.JooqPlugin
import sbtjooq.checker.JooqCheckerKeys.*
import sbtjooq.checker.internal.JooqWarts
import wartremover.WartRemover
import wartremover.WartRemover.autoImport.*

object JooqCheckerPlugin extends AutoPlugin {

  override def requires: Plugins = JooqPlugin && WartRemover

  object autoImport extends JooqCheckerKeys {

    final val JooqCheckerLevel = sbtjooq.checker.JooqCheckerLevel

    @deprecated("Use JooqCheckerLevel instead", "0.8.0")
    final val CheckerLevel = sbtjooq.checker.JooqCheckerLevel

  }

  override def globalSettings: Seq[Setting[?]] = Seq(
    jooqCheckerLevelPlainSQL := JooqCheckerLevel.Error,
    jooqCheckerLevelSQLDialect := JooqCheckerLevel.Error
  )

  override def projectConfigurations: Seq[Configuration] = Seq(JooqChecker)

  override def projectSettings: Seq[Setting[?]] =
    jooqCheckerDefaultSettings ++
      inConfig(Compile)(inTask(compile)(jooqCheckerSettings))

  private def jooqCheckerDefaultSettings: Seq[Setting[?]] = Seq(
    libraryDependencies +=
      ("com.github.kxbmap" %% "sbt-jooq-checker-tool" % BuildInfo.sbtJooqVersion % JooqChecker).intransitive()
  ) ++
    JooqPlugin.jooqDependencies(JooqChecker) ++
    inConfig(JooqChecker)(
      Seq(
        managedClasspath := Classpaths.managedJars(JooqChecker, classpathTypes.value, update.value)
      )
    )

  lazy val jooqCheckerSettings: Seq[Setting[?]] = Seq(
    wartremoverClasspaths ++= (JooqChecker / managedClasspath).value.files.map(_.toURI.toString),
    wartremoverErrors :=
      wartremoverErrors.value.filterNot(JooqWarts.all) ++
        JooqWarts.errors(jooqCheckerLevelPlainSQL.value, jooqCheckerLevelSQLDialect.value),
    wartremoverWarnings :=
      wartremoverWarnings.value.filterNot(JooqWarts.all) ++
        JooqWarts.warnings(jooqCheckerLevelPlainSQL.value, jooqCheckerLevelSQLDialect.value)
  )

}
