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

val scalaVersion = "2.13.2"
val jooqVersion = "3.13.2"
val h2Version = "1.4.200"

lazy val scriptedSettings = Seq(
  scriptedSbt := sbtVersion.value,
  scriptedBufferLog := false,
  scriptedLaunchOpts ++= Seq(
    "-Xmx1024M",
    s"-Dplugin.version=${version.value}",
    s"-Dscala.version=$scalaVersion",
    s"-Djooq.version=$jooqVersion",
    s"-Dh2.version=$h2Version",
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
      "JOOQ_VERSION" -> jooqVersion,
      "JOOQ_MINOR_VERSION" -> (jooqVersion match {
        case VersionNumber(x +: y +: _, _, _) => s"$x.$y"
      }),
      "H2_VERSION" -> h2Version,
    ),
    libraryDependencies += sbtDependency.value
  )

val updateReadme = taskKey[Unit]("Update README.md")
val readmeFile = "README.md"
updateReadme := IO.copyFile((docs / mdocOut).value / readmeFile, baseDirectory.value / readmeFile)
updateReadme := updateReadme.dependsOn((docs / mdoc).toTask(s" --include $readmeFile")).value
