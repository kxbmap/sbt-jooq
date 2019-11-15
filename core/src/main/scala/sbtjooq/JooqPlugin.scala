package sbtjooq

import sbt.Keys._
import sbt._
import sbtjooq.JooqKeys._

object JooqPlugin extends AutoPlugin {

  val DefaultJooqVersion = "3.12.3"

  object autoImport extends JooqKeys {

    def addJooqSettingsTo(config: Configuration): Seq[Setting[_]] =
      jooqScopedSettings(config)

  }

  override def globalSettings: Seq[Setting[_]] = Seq(
    jooqVersion := DefaultJooqVersion,
    jooqOrganization := "org.jooq",
    jooqModules := Seq("jooq"),
    autoJooqLibrary := true
  )

  override def projectSettings: Seq[Setting[_]] = jooqScopedSettings(Compile)

  def jooqScopedSettings(config: Configuration): Seq[Setting[_]] = Seq(
    libraryDependencies ++= {
      if ((config / autoJooqLibrary).value)
        (config / jooqModules).value.map((config / jooqOrganization).value % _ % (config / jooqVersion).value % config)
      else
        Nil
    }
  )

}
