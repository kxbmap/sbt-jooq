package sbtjooq

import sbt.Keys._
import sbt._
import sbtjooq.JooqKeys._

object JooqPlugin extends AutoPlugin {

  object autoImport extends JooqKeys

  override def globalSettings: Seq[Setting[_]] = Seq(
    jooqVersion := BuildInfo.defaultJooqVersion,
    jooqOrganization := "org.jooq",
    jooqModules := Seq("jooq"),
    autoJooqLibrary := true
  )

  override def projectSettings: Seq[Setting[_]] =
    jooqDependencies(Compile)

  def jooqDependencies(config: Configuration): Seq[Setting[_]] = Seq(
    libraryDependencies ++= {
      if ((config / autoJooqLibrary).value)
        (config / jooqModules).value.map((config / jooqOrganization).value % _ % (config / jooqVersion).value % config)
      else
        Nil
    }
  )

}
