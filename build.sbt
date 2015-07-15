sbtPlugin := true

name := "sbt-jooq"
version := "0.2.0-SNAPSHOT"
description := "jOOQ plugin for SBT 0.13.5+"
organization := "com.github.kxbmap"

scalacOptions ++= Seq(
  "-deprecation",
  "-unchecked",
  "-feature",
  "-Xlint"
)

Publish.settings
