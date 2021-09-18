name := "sbt-jooq"

publish / skip := true

inThisBuild(Seq(
  scalacOptions ++= Seq(
    "-deprecation",
    "-unchecked",
    "-feature",
    "-Xlint"
  ),

  // https://github.com/sbt/sbt/issues/5049
  pluginCrossBuild / sbtVersion := "1.2.8"
))

import Versions._

lazy val core = project
  .enablePlugins(SbtPlugin, BuildInfoPlugin)
  .settings(
    name := "sbt-jooq-core",
    buildInfoKeys := Seq[BuildInfoKey]("defaultJooqVersion" -> jooqVersion),
    buildInfoPackage := "sbtjooq"
  )

lazy val codegen = project
  .dependsOn(core)
  .enablePlugins(SbtPlugin, BuildInfoPlugin)
  .settings(
    name := "sbt-jooq-codegen",
    scripted := scripted.dependsOn(Def.task {
      (core / publishLocal).value
      (codegenTool / publishLocal).value
    }).evaluated,
    libraryDependencies += "com.lihaoyi" %% "fastparse" % fastParseVersion,
    buildInfoKeys := Seq[BuildInfoKey](
      "sbtJooqVersion" -> version.value,
      "javaxActivationVersion" -> javaxActivationVersion,
      "jaxbApiVersion" -> jaxbApiVersion,
      "jaxbCoreVersion" -> jaxbCoreVersion,
      "jaxbImplVersion" -> jaxbImplVersion,
      "javaxAnnotationApiVersion" -> javaxAnnotationApiVersion
    ),
    buildInfoPackage := "sbtjooq.codegen",

    TaskKey[Unit]("disableIncompatibleTestsWithEarlierThanJava11") := {
      val log = state.value.log
      val disabled = ScriptedSettings.disableScriptedTests(sbtTestDirectory.value) { name =>
        val m = """jooq-codegen-compat/jooq-(\d+\.\d+)""".r.pattern.matcher(name)
        m.matches() && VersionNumber(m.group(1)).matchesSemVer(SemanticSelector(">=3.15"))
      }
      disabled.foreach(p => log.warn(s"Scripted test disabled: $p"))
    }
  )

lazy val codegenTool = project
  .in(file("codegen-tool"))
  .settings(
    name := "sbt-jooq-codegen-tool",
    crossPaths := false,
    autoScalaLibrary := false,
    libraryDependencies ++= Seq(
      "ch.qos.logback" % "logback-classic" % logbackVersion
    )
  )

lazy val checker = project
  .dependsOn(core)
  .enablePlugins(SbtPlugin, BuildInfoPlugin)
  .settings(
    name := "sbt-jooq-checker",
    scripted := scripted.dependsOn(core / publishLocal).evaluated,
    addSbtPlugin("org.wartremover" % "sbt-wartremover" % sbtWartRemoverVersion),
    buildInfoKeys := Seq[BuildInfoKey]("defaultJooqWartsVersion" -> jooqWartsVersion),
    buildInfoPackage := "sbtjooq.checker"
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
    scalacOptions ++= Seq(
      "-Wconf:cat=unused-nowarn:s",
    ),
    libraryDependencies += sbtDependency.value
  )

val readmeFile = "README.md"
TaskKey[Unit]("updateReadme") :=
  Def.sequential(
    (docs / mdoc).toTask(s" --include $readmeFile"),
    Def.task(IO.copyFile((docs / mdocOut).value / readmeFile, baseDirectory.value / readmeFile)),
  ).value
