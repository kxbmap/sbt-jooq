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

import Versions._

lazy val scriptedSettings = Seq(
  scriptedSbt := sbtVersion.value,
  scriptedBufferLog := false,
  scriptedLaunchOpts ++= Seq(
    "-Xmx1024M",
    s"-Dplugin.version=${version.value}",
    s"-Dscala.version=$scalaVersion_",
    s"-Djooq.version=$jooqVersion",
    s"-Dh2.version=$h2Version",
  )
)

lazy val core = project
  .enablePlugins(SbtPlugin, BuildInfoPlugin)
  .settings(
    name := "sbt-jooq-core",
    scriptedSettings,
    buildInfoKeys := Seq[BuildInfoKey]("defaultJooqVersion" -> jooqVersion),
    buildInfoPackage := "sbtjooq"
  )

lazy val codegen = project
  .dependsOn(core)
  .enablePlugins(SbtPlugin)
  .settings(
    name := "sbt-jooq-codegen",
    scriptedSettings,
    scripted := scripted.dependsOn(core / publishLocal).evaluated,
    addSbtPlugin("com.github.kxbmap" % "sbt-slf4j-simple" % sbtSlf4jSimpleVersion),
    libraryDependencies += "com.lihaoyi" %% "fastparse" % fastParseVersion
  )

lazy val checker = project
  .dependsOn(core)
  .enablePlugins(SbtPlugin)
  .settings(
    name := "sbt-jooq-checker",
    scriptedSettings,
    scripted := scripted.dependsOn(core / publishLocal).evaluated,
    addSbtPlugin("org.wartremover" % "sbt-wartremover" % sbtWartRemoverVersion)
  )

lazy val docs = project
  .dependsOn(codegen, checker)
  .enablePlugins(MdocPlugin)
  .settings(
    publish / skip := true,
    mdocIn := (Compile / sourceDirectory).value / "mdoc",
    mdocVariables ++= Map(
      "VERSION" -> version.value,
      "JOOQ_VERSION" -> jooqVersion,
      "JOOQ_MINOR_VERSION" -> minorVersion(jooqVersion),
      "H2_VERSION" -> h2Version,
    ),
    libraryDependencies += sbtDependency.value
  )

val updateReadme = taskKey[Unit]("Update README.md")
val readmeFile = "README.md"
updateReadme := IO.copyFile((docs / mdocOut).value / readmeFile, baseDirectory.value / readmeFile)
updateReadme := updateReadme.dependsOn((docs / mdoc).toTask(s" --include $readmeFile")).value
