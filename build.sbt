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
  scriptedSbt := sbtVersion.value,
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
    addSbtPlugin("org.wartremover" % "sbt-wartremover" % "2.4.8")
  )

lazy val docs = project
  .dependsOn(codegen, checker)
  .enablePlugins(MdocPlugin)
  .settings(
    publish / skip := true,
    mdocIn := (Compile / sourceDirectory).value / "mdoc",
    mdocVariables ++= Map(
      "VERSION" -> version.value,
      "JOOQ_VERSION" -> "3.13.2",
      "JOOQ_MINOR_VERSION" -> "3.13",
      "H2_VERSION" -> "1.4.200",
    ),
    libraryDependencies += sbtDependency.value
  )

val updateReadme = taskKey[Unit]("Update README.md")
val readmeFile = "README.md"
updateReadme := IO.copyFile((docs / mdocOut).value / readmeFile, baseDirectory.value / readmeFile)
updateReadme := updateReadme.dependsOn((docs / mdoc).toTask(s" --include $readmeFile")).value
