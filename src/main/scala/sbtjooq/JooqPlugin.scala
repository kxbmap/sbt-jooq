package sbtjooq

import sbt.Keys._
import sbt._
import sbtjooq.JooqKeys._

object JooqPlugin extends AutoPlugin {

  val DefaultJooqVersion = "3.11.5"

  object autoImport extends JooqKeys {

    def addJooqSettingsTo(config: Configuration): Seq[Setting[_]] =
      jooqScopedSettings(config)

  }

  override def globalSettings: Seq[Setting[_]] = Seq(
    jooqVersion := DefaultJooqVersion,
    jooqOrganization := "org.jooq",
    autoJooqLibrary := true
  )

  override def projectSettings: Seq[Setting[_]] = jooqScopedSettings(Compile)

  def jooqScopedSettings(config: Configuration): Seq[Setting[_]] = Seq(
    libraryDependencies ++= {
      if ((config / autoJooqLibrary).value)
        Seq((config / jooqOrganization).value % "jooq" % (config / jooqVersion).value % config)
      else
        Nil
    }
  )

}
