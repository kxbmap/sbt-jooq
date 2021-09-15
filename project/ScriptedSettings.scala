import Versions._
import java.nio.file.{Files, Path}
import sbt.Keys._
import sbt.ScriptedPlugin.autoImport._
import sbt._
import sbt.plugins.SbtPlugin

object ScriptedSettings extends AutoPlugin {

  override def requires: Plugins = SbtPlugin

  override def trigger: PluginTrigger = allRequirements

  object autoImport {
    val disableIncompatibleTests = taskKey[Unit]("")
  }

  import autoImport._

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
    },
    disableIncompatibleTests := disableIncompatibleTestsTask.value
  )

  private def javaVersion = sys.props("java.version").takeWhile(_.isDigit).toInt

  private def correspondingJooqVersion =
    if (javaVersion < 11)
      jooqVersionForJava8
    else
      jooqVersion

  private def disableIncompatibleTestsTask = Def.task[Unit] {
    if (javaVersion < 11) {
      def isTarget(name: String): Boolean = {
        val m = """jooq-(\d+\.\d+)""".r.pattern.matcher(name)
        m.matches() && VersionNumber(m.group(1)).matchesSemVer(SemanticSelector(">=3.15"))
      }
      def disableTest(path: Path): Unit = {
        val disabled = path / "disabled"
        if (Files.notExists(disabled)) Files.createFile(disabled)
      }
      val dir = sbtTestDirectory.value.toPath
      if (Files.isDirectory(dir))
        Files.list(dir)
          .filter(Files.isDirectory(_))
          .flatMap(Files.list(_))
          .filter(Files.isDirectory(_))
          .filter(path => isTarget(path.getFileName.toString))
          .forEach(disableTest)
    }
  }

}
