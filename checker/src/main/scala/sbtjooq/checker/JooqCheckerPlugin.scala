package sbtjooq.checker

import sbt.Keys._
import sbt._
import sbtjooq.JooqPlugin
import sbtjooq.checker.JooqCheckerKeys._
import sbtjooq.checker.internal.JooqWarts
import wartremover.WartRemover
import wartremover.WartRemover.autoImport._

object JooqCheckerPlugin extends AutoPlugin {

  override def requires: Plugins = JooqPlugin && WartRemover

  object autoImport extends JooqCheckerKeys {

    val CheckerLevel = sbtjooq.checker.CheckerLevel

    def addJooqCheckerSettingsTo(config: Configuration): Seq[Setting[_]] =
      jooqCheckerScopedSettings(config)

    def addJooqCheckerSettingsTo(config: Configuration, task: Scoped): Seq[Setting[_]] =
      jooqCheckerScopedSettings(config, task)

  }

  override def globalSettings: Seq[Setting[_]] = Seq(
    jooqCheckerLevels := CheckerLevels.default,
    jooqCheckerJooqWartsVersion := JooqWarts.DefaultVersion
  )

  override def projectConfigurations: Seq[Configuration] = Seq(JooqChecker)

  override def projectSettings: Seq[Setting[_]] =
    jooqCheckerDefaultSettings ++ jooqCheckerScopedSettings(Compile, compile)

  def jooqCheckerDefaultSettings: Seq[Setting[_]] = Seq(
    libraryDependencies += ("com.github.kxbmap" %% "jooq-warts" % jooqCheckerJooqWartsVersion.value % JooqChecker).intransitive(),
  ) ++
    JooqPlugin.jooqScopedSettings(JooqChecker) ++
    inConfig(JooqChecker)(Seq(
      managedClasspath := Classpaths.managedJars(JooqChecker, classpathTypes.value, update.value)
    ))

  def jooqCheckerScopedSettings(config: Configuration): Seq[Setting[_]] =
    inConfig(config)(Seq(
      wartremoverClasspaths ++= (JooqChecker / managedClasspath).value.files.map(_.toURI.toString),
      wartremoverErrors := wartremoverErrors.value.filterNot(JooqWarts.all) ++ jooqCheckerLevels.value.errors,
      wartremoverWarnings := wartremoverWarnings.value.filterNot(JooqWarts.all) ++ jooqCheckerLevels.value.warnings
    ))

  def jooqCheckerScopedSettings(config: Configuration, task: Scoped): Seq[Setting[_]] =
    inTask(task)(jooqCheckerScopedSettings(config))

}
