import com.jsuereth.sbtpgp.PgpKeys.*
import com.jsuereth.sbtpgp.SbtPgp
import sbt.*
import sbt.Keys.*
import sbtrelease.ReleasePlugin
import sbtrelease.ReleasePlugin.autoImport.*
import xerial.sbt.Sonatype
import xerial.sbt.Sonatype.GitHubHosting
import xerial.sbt.Sonatype.SonatypeKeys.*

object PublishSettings extends AutoPlugin {

  override def requires: Plugins = Sonatype && SbtPgp && ReleasePlugin

  override def trigger: PluginTrigger = allRequirements

  override def buildSettings: Seq[Setting[?]] = Seq(
    description := "jOOQ plugin for sbt 1.3+",
    organization := "com.github.kxbmap",
    organizationName := "Tsukasa Kitachi",
    startYear := Some(2015),
    licenses := Seq(License.Apache2),
    versionScheme := Some("early-semver")
  )

  override def projectSettings: Seq[Setting[?]] = Seq(
    publishMavenStyle := true,
    publishTo := sonatypePublishToBundle.value,
    sonatypeProjectHosting := Some(GitHubHosting("kxbmap", "sbt-jooq", "Tsukasa Kitachi", "kxbmap@gmail.com")),
    pomIncludeRepository := { _ => false },
    releasePublishArtifactsAction := publishSigned.value
  )

}
