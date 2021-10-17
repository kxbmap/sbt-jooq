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
    jooqCheckerJooqWartsVersion := BuildInfo.defaultJooqWartsVersion
  )

  override def projectConfigurations: Seq[Configuration] = Seq(JooqChecker)

  override def projectSettings: Seq[Setting[_]] =
    jooqCheckerDefaultSettings ++
      inConfig(Compile)(inTask(compile)(jooqCheckerSettings))

  private def jooqCheckerDefaultSettings: Seq[Setting[_]] = Seq(
    libraryDependencies +=
      ("com.github.kxbmap" %% "jooq-warts" % jooqCheckerJooqWartsVersion.value % JooqChecker).intransitive(),
  ) ++
    JooqPlugin.jooqDependencies(JooqChecker) ++
    inConfig(JooqChecker)(Seq(
      managedClasspath := Classpaths.managedJars(JooqChecker, classpathTypes.value, update.value)
    ))

  lazy val jooqCheckerSettings: Seq[Setting[_]] = Seq(
    wartremoverClasspaths ++= (JooqChecker / managedClasspath).value.files.map(_.toURI.toString),
    wartremoverErrors := wartremoverErrors.value.filterNot(JooqWarts.all) ++ jooqCheckerLevels.value.errors,
    wartremoverWarnings := wartremoverWarnings.value.filterNot(JooqWarts.all) ++ jooqCheckerLevels.value.warnings
  )

}
