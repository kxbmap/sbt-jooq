import Versions._
import java.nio.file.{Files, Path}
import java.util.stream.Collectors._
import sbt.Keys._
import sbt.ScriptedPlugin.autoImport._
import sbt._
import sbt.plugins.SbtPlugin
import scala.jdk.CollectionConverters._

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
      s"-Djooq.version=$jooqVersion",
      s"-Dh2.version=$h2Version",
      s"-Dflywaysbt.version=$flywaySbtVersion"
    ) ++ jooqVersions.map { v =>
      s"-Djooq.${minorVersion(v).replace('.', '_')}.version=$v"
    }
  )

  def disableScriptedTests(testDirectory: File)(isTarget: String => Boolean): List[Path] = {
    def name(path: Path): String = {
      val c = path.getNameCount
      path.asScala.drop(c - 2).mkString("/")
    }
    def disableTest(path: Path): Path = {
      val disabled = path / "disabled"
      if (Files.notExists(disabled)) Files.createFile(disabled)
      path
    }
    val dir = testDirectory.toPath
    if (Files.isDirectory(dir))
      Files.list(dir)
        .filter(Files.isDirectory(_))
        .flatMap[Path](Files.list(_))
        .filter(Files.isDirectory(_))
        .filter(path => isTarget(name(path)))
        .map[Path](disableTest)
        .collect(toList[Path]).asScala.toList
    else
      Nil
  }

}
