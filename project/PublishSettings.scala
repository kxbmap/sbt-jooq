import sbt.Keys._
import sbt._
import xerial.sbt.Sonatype
import xerial.sbt.Sonatype.GitHubHosting
import xerial.sbt.Sonatype.SonatypeKeys._

object PublishSettings extends AutoPlugin {

  override def requires: Plugins = Sonatype

  override def trigger: PluginTrigger = allRequirements

  override def projectSettings: Seq[Setting[_]] = Seq(
    description := "jOOQ plugin for sbt 1.0+",
    organization := "com.github.kxbmap",

    publishMavenStyle := true,
    publishTo := sonatypePublishToBundle.value,
    licenses := Seq(
      "Apache License, Version 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.html")
    ),
    sonatypeProjectHosting := Some(GitHubHosting("kxbmap", "sbt-jooq", "Tsukasa Kitachi", "kxbmap@gmail.com")),
    pomIncludeRepository := { _ => false }
  )

}
