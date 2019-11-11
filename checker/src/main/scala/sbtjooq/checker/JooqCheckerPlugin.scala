package sbtjooq.checker

import sbt.Keys._
import sbt._
import sbtjooq.JooqPlugin
import sbtjooq.checker.JooqCheckerKeys._
import wartremover._

object JooqCheckerPlugin extends AutoPlugin {

  override def requires: Plugins = JooqPlugin && WartRemover

  object autoImport extends JooqCheckerKeys {

    def addJooqCheckerSettingsTo(config: Configuration): Seq[Setting[_]] =
      jooqCheckerScopedSettings(config)

  }

  override def projectConfigurations: Seq[Configuration] = Seq(JooqChecker)

  override def projectSettings: Seq[Setting[_]] =
    jooqCheckerDefaultSettings ++ jooqCheckerScopedSettings(Compile)

  def jooqCheckerDefaultSettings: Seq[Setting[_]] = Seq(
    jooqCheckerJooqWartsVersion := JooqWarts.DefaultVersion,
    libraryDependencies += ("com.github.kxbmap" %% "jooq-warts" % jooqCheckerJooqWartsVersion.value % JooqChecker).intransitive(),
  ) ++
    JooqPlugin.jooqScopedSettings(JooqChecker) ++
    inConfig(JooqChecker)(Seq(
      managedClasspath := Classpaths.managedJars(JooqChecker, classpathTypes.value, update.value)
    ))

  def jooqCheckerScopedSettings(config: Configuration): Seq[Setting[_]] =
    inConfig(config)(Seq(
      wartremoverClasspaths in compile ++= (JooqChecker / managedClasspath).value.files.map(_.toURI.toString),
      wartremoverErrors in compile ++= Seq(JooqWarts.PlainSQL, JooqWarts.SQLDialect)
    ))

}
