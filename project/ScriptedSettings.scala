import Versions._
import sbt.Keys._
import sbt.ScriptedPlugin.autoImport._
import sbt._
import sbt.plugins.SbtPlugin

object ScriptedSettings extends AutoPlugin {

  override def requires: Plugins = SbtPlugin

  override def trigger: PluginTrigger = allRequirements

  override def projectSettings: Seq[Setting[_]] = Seq(
    scriptedSbt := sbtVersion.value,
    scriptedBufferLog := false,
    scriptedLaunchOpts ++= Seq(
      "-Xmx1024M",
      s"-Dplugin.version=${version.value}",
      s"-Dscala.version=$scriptedScalaVersion",
      s"-Djooq.version=$correspondingJooqVersion",
      s"-Dh2.version=$h2Version",
      s"-Dflywaysbt.version=$flywaySbtVersion"
    ) ++ jooqVersions.map { v =>
      s"-Djooq.${minorVersion(v).replace('.', '_')}.version=$v"
    }
  )

  private def javaVersion = sys.props("java.version").takeWhile(_.isDigit).toInt

  private def correspondingJooqVersion =
    if (javaVersion < 11)
      jooqVersionForJava8
    else
      jooqVersion

}
