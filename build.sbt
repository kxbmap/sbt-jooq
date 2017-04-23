sbtPlugin := true

name := "sbt-jooq"
description := "jOOQ plugin for SBT 0.13.5+"
organization := "com.github.kxbmap"

scalacOptions ++= Seq(
  "-deprecation",
  "-unchecked",
  "-feature",
  "-Xlint"
)
