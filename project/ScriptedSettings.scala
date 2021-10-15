import Versions._
import sbt._
import sbt.Keys._
import sbt.ScriptedPlugin.autoImport._
import sbt.plugins.SbtPlugin

object ScriptedSettings extends AutoPlugin {

  override def requires: Plugins = SbtPlugin

  override def trigger: PluginTrigger = allRequirements

  override def projectSettings: Seq[Setting[_]] = Seq(
    scriptedSbt := sbtVersion.value,
    scriptedBufferLog := false,
    scriptedLaunchOpts ++= Seq(
      "-Xmx1024M",
      s"-Dscripted.plugin.version=${version.value}",
      s"-Dscripted.scala.version=$scriptedScalaVersion",
      s"-Dscripted.jooq.version=$jooqVersion",
      s"-Dscripted.h2.version=$h2Version",
      s"-Dscripted.flywaysbt.version=$flywaySbtVersion"
    ) ++ jooqVersions.map { v =>
      s"-Dscripted.jooq.${minorVersion(v).replace('.', '_')}.version=$v"
    }
  )

}
