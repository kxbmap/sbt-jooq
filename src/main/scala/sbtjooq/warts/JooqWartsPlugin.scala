package sbtjooq.warts

import sbt.Keys._
import sbt._
import sbtjooq.JooqPlugin
import sbtjooq.warts.JooqWartsKeys._
import wartremover._

object JooqWartsPlugin extends AutoPlugin {

  val DefaultJooqWartsVersion = "0.1.0"

  override def requires: Plugins = JooqPlugin && WartRemover

  object autoImport extends JooqWartsKeys {

    def addJooqWartsSettingsTo(config: Configuration): Seq[Setting[_]] =
      jooqWartsScopedSettings(config)

  }

  override def projectConfigurations: Seq[Configuration] = Seq(JooqWarts)

  override def projectSettings: Seq[Setting[_]] =
    jooqWartsDefaultSettings ++ jooqWartsScopedSettings(Compile)

  def jooqWartsDefaultSettings: Seq[Setting[_]] = Seq(
    jooqWartsVersion := DefaultJooqWartsVersion,
    libraryDependencies += ("com.github.kxbmap" %% "jooq-warts" % jooqWartsVersion.value % JooqWarts).intransitive(),
  ) ++
    JooqPlugin.jooqScopedSettings(JooqWarts) ++
    inConfig(JooqWarts)(Seq(
      managedClasspath := Classpaths.managedJars(JooqWarts, classpathTypes.value, update.value)
    ))

  def jooqWartsScopedSettings(config: Configuration): Seq[Setting[_]] =
    inConfig(config)(Seq(
      wartremoverClasspaths in compile ++= (JooqWarts / managedClasspath).value.files.map(_.toURI.toString),
      wartremoverErrors in compile ++= Seq(JooqWart.PlainSQL, JooqWart.SQLDialect)
    ))

}
