import sbt.Keys._
import sbt._
import xerial.sbt.Sonatype
import xerial.sbt.Sonatype.GitHubHosting
import xerial.sbt.Sonatype.SonatypeKeys._

object Publish extends AutoPlugin {

  override def requires: Plugins = Sonatype

  override def trigger: PluginTrigger = allRequirements

  override lazy val projectSettings: Seq[Setting[_]] = Seq(
    publishMavenStyle := true,
    publishTo := sonatypePublishTo.value,
    licenses := Seq(
      "Apache License, Version 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.html")
    ),
    sonatypeProjectHosting := Some(GitHubHosting("kxbmap", "configs", "Tsukasa Kitachi", "kxbmap@gmail.com")),
    pomIncludeRepository := { _ => false }
  )

}
