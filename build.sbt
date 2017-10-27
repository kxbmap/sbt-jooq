sbtPlugin := true

name := "sbt-jooq"
description := "jOOQ plugin for SBT 0.13.5+"
organization := "com.github.kxbmap"

crossSbtVersions := Seq("1.0.3", "0.13.16")

scalacOptions ++= Seq(
  "-deprecation",
  "-unchecked",
  "-feature",
  "-Xlint"
)
