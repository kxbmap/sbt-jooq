import com.jsuereth.sbtpgp.PgpKeys._
import com.jsuereth.sbtpgp.SbtPgp
import sbt._
import sbt.Keys._
import sbtrelease.ReleasePlugin
import sbtrelease.ReleasePlugin.autoImport._
import xerial.sbt.Sonatype
import xerial.sbt.Sonatype.GitHubHosting
import xerial.sbt.Sonatype.SonatypeKeys._

object PublishSettings extends AutoPlugin {

  override def requires: Plugins = Sonatype && SbtPgp && ReleasePlugin

  override def trigger: PluginTrigger = allRequirements

  override def buildSettings: Seq[Setting[_]] = Seq(
    description := "jOOQ plugin for sbt 1.3+",
    organization := "com.github.kxbmap",
    versionScheme := Some("early-semver"),
    licenses := Seq(
      "Apache License, Version 2.0" -> url("https://www.apache.org/licenses/LICENSE-2.0.html")
    ),
  )

  override def projectSettings: Seq[Setting[_]] = Seq(
    publishMavenStyle := true,
    publishTo := sonatypePublishToBundle.value,
    sonatypeProjectHosting := Some(GitHubHosting("kxbmap", "sbt-jooq", "Tsukasa Kitachi", "kxbmap@gmail.com")),
    pomIncludeRepository := { _ => false },
    releasePublishArtifactsAction := publishSigned.value,
  )

}
