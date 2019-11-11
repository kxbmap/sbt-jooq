name := "sbt-jooq"
description := "jOOQ plugin for sbt 1.0+"
organization := "com.github.kxbmap"

enablePlugins(SbtPlugin)

scalacOptions ++= Seq(
  "-deprecation",
  "-unchecked",
  "-feature",
  "-Xlint"
)
scalacOptions in (Compile, console) += "-Xlint:-unused"

addSbtPlugin("com.github.kxbmap" % "sbt-slf4j-simple" % "0.2.0")
addSbtPlugin("org.wartremover" % "sbt-wartremover" % "2.4.3")
