sbtPlugin := true

name := "sbt-jooq"
description := "jOOQ plugin for SBT 0.13.5+"
version := "0.1.0-SNAPSHOT"
organization := "com.github.kxbmap"

scalacOptions ++= Seq(
  "-deprecation",
  "-unchecked",
  "-feature",
  "-Xlint"
)

publishSettings

lazy val publishSettings = Seq(
  publishMavenStyle := true,
  publishTo := {
    if (isSnapshot.value)
      Some(Opts.resolver.sonatypeSnapshots)
    else
      Some(Opts.resolver.sonatypeStaging)
  },
  licenses := Seq(
    "Apache License, Version 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.html")
  ),
  scmInfo := Some(ScmInfo(
    browseUrl = url("http://github.com/kxbmap/sbt-jooq"),
    connection = "scm:git:git@github.com:kxbmap/sbt-jooq.git"
  )),
  homepage := Some(url("http://github.com/kxbmap/sbt-jooq")),
  organizationHomepage := Some(url("http://github.com/kxbmap")),
  pomIncludeRepository := { _ => false },
  pomExtra :=
    <developers>
      <developer>
        <id>kxbmap</id>
        <name>Tsukasa Kitachi</name>
        <url>http://github.com/kxbmap</url>
      </developer>
    </developers>
)
