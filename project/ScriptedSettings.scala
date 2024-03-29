import Versions.*
import sbt.*
import sbt.Keys.*
import sbt.ScriptedPlugin.autoImport.*
import sbt.plugins.SbtPlugin

object ScriptedSettings extends AutoPlugin {

  override def requires: Plugins = SbtPlugin

  override def trigger: PluginTrigger = allRequirements

  object autoImport {

    val scriptedScalaVersion = settingKey[String]("Scala version used by scripted projects")

  }

  import autoImport.*

  override def projectSettings: Seq[Setting[?]] = Seq(
    scriptedSbt := sbtVersion.value,
    scriptedScalaVersion := sys.env.getOrElse("SCRIPTED_SCALA", scala3),
    scriptedLaunchOpts ++= Seq(
      "-Xmx1024M",
      s"-Dscripted.plugin.version=${version.value}",
      s"-Dscripted.scala.version=${scriptedScalaVersion.value}",
      s"-Dscripted.jooq.version=$jooqVersion",
      s"-Dscripted.h2.version=$h2Version",
      s"-Dscripted.h2.v1.version=$h2V1Version",
      s"-Dscripted.flywaysbt.version=$flywaySbtVersion"
    ) ++ jooqVersions.map { v =>
      s"-Dscripted.jooq.${minorVersion(v).replace('.', '_')}.version=$v"
    },
    scripted / javaHome := sys.env.get("SCRIPTED_JAVA_HOME").map(file),
    scriptedBatchExecution := insideCI.value && ciTaskIs("codegen"),
    scriptedParallelInstances := 2,
    scriptedBufferLog := insideCI.value && !ciTaskIs("compat")
  )

  private def ciTaskIs(task: String) = sys.env.get("CI_TASK").contains(task)

  lazy val scriptedJdbcUrls =
    scriptedLaunchOpts ++= {
      val urls = (0 to 1).map { n =>
        val testFiles = sourceDirectory.value / "sbt-test-files"
        val setup = (testFiles / "sql" / s"setup$n.sql").toString.replace('\\', '/')
        n -> s"jdbc:h2:mem:;INIT=runscript from '$setup'"
      }
      s"-Dscripted.jdbc.url=${urls.head._2}" +:
        urls.map {
          case (n, url) => s"-Dscripted.jdbc.url.$n=$url"
        }
    }

}
