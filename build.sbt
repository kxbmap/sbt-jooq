name := "sbt-jooq"
description in ThisBuild := "jOOQ plugin for sbt 1.0+"
organization in ThisBuild := "com.github.kxbmap"

publish / skip := true

inThisBuild(Seq(
  scalacOptions ++= Seq(
    "-deprecation",
    "-unchecked",
    "-feature",
    "-Xlint"
  ),
  Compile / console / scalacOptions += "-Xlint:-unused",

  // https://github.com/sbt/sbt/issues/5049
  pluginCrossBuild / sbtVersion := "1.2.8"
))

lazy val scriptedSettings = Seq(
  scriptedBufferLog := false,
  scriptedLaunchOpts ++= Seq(
    "-Xmx1024M",
    s"-Dplugin.version=${version.value}"
  )
)

lazy val core = project
  .enablePlugins(SbtPlugin)
  .settings(
    name := "sbt-jooq-core",
    scriptedSettings
  )

lazy val codegen = project
  .dependsOn(core)
  .enablePlugins(SbtPlugin)
  .settings(
    name := "sbt-jooq-codegen",
    scriptedSettings,
    scripted := scripted.dependsOn(core / publishLocal).evaluated,
    addSbtPlugin("com.github.kxbmap" % "sbt-slf4j-simple" % "0.2.0")
  )

lazy val checker = project
  .dependsOn(core)
  .enablePlugins(SbtPlugin)
  .settings(
    name := "sbt-jooq-checker",
    scriptedSettings,
    scripted := scripted.dependsOn(core / publishLocal).evaluated,
    addSbtPlugin("org.wartremover" % "sbt-wartremover" % "2.4.3")
  )
