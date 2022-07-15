import com.jsuereth.sbtpgp.SbtPgp
import mdoc.MdocPlugin.autoImport._
import sbt._
import sbt.Def.Initialize
import sbt.Keys._
import sbtrelease.ReleasePlugin
import sbtrelease.ReleasePlugin.autoImport._
import sbtrelease.ReleaseStateTransformations._
import sbtrelease.Version.Bump
import xerial.sbt.Sonatype

object ReleaseSettings extends AutoPlugin {

  override def requires: Plugins = Sonatype && ReleasePlugin && SbtPgp

  object autoImport {
    val readmeFile = settingKey[File]("Readme file name")
    val updateReadme = taskKey[Unit]("Update readme file")
  }

  import autoImport._

  override def projectSettings: Seq[Setting[_]] = Seq(
    readmeFile := baseDirectory.value / "README.md",
    updateReadme := updateReadmeTask.value,
    releaseVersionBump := Bump.Minor,
    releaseProcess := Seq(
      checkSnapshotDependencies,
      inquireVersions,
      setReleaseVersion,
      runClean,
      releaseStepCommandAndRemaining("+headerCheckAll"),
      releaseStepCommandAndRemaining("+scalafmtCheckAll"),
      releaseStepCommand("scalafmtSbtCheck"),
      releaseStepCommandAndRemaining("+test"),
      releaseStepCommand("scripted"),
      releaseStepTask(updateReadme),
      commitReadme,
      commitReleaseVersion,
      tagRelease,
      releaseStepCommandAndRemaining("+publishSigned"),
      releaseStepCommand("sonatypeBundleRelease"),
      setNextVersion,
      commitNextVersion,
      releaseStepCommand("reload")
    )
  )

  private val docs = LocalProject("docs")

  private def updateReadmeTask: Initialize[Task[Unit]] =
    Def.taskDyn {
      val readme = readmeFile.value
      val name = readme.getName
      Def.sequential(
        (docs / mdoc).toTask(s" --include $name"),
        Def.task[Unit] {
          val src = (docs / mdocOut).value / name
          IO.copy(Seq(src -> readme))
        }
      )
    }

  private val commitReadme = ReleaseStep { st =>
    val x = Project.extract(st)
    val vcs = x.get(releaseVcs).getOrElse(
      sys.error("Aborting release. Working directory is not a repository of a recognized VCS.")
    )
    val sign = x.get(releaseVcsSign)
    val signOff = x.get(releaseVcsSignOff)
    val readme = x.get(readmeFile)
    val name = readme.getName
    vcs.add(readme.getPath) ! st.log
    val status = vcs.status.!!.trim
    if (status.contains(name)) {
      vcs.commit(s"Update $name", sign, signOff) ! st.log
    }
    st
  }

}
