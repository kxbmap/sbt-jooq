import ProjectUtil.*
import Versions.*

name := "sbt-jooq"

publish / skip := true

enablePlugins(ReleaseSettings)

lazy val core = project
  .enablePlugins(SbtPlugin, BuildInfoPlugin)
  .settings(
    name := "sbt-jooq-core",
    buildInfoKeys := Seq[BuildInfoKey](
      "defaultJooqVersion" -> jooqVersion
    ),
    buildInfoPackage := "sbtjooq"
  )

lazy val codegen = project
  .dependsOn(core)
  .enablePlugins(SbtPlugin, BuildInfoPlugin)
  .settings(
    name := "sbt-jooq-codegen",
    scripted := scripted.dependsOn(core / publishLocal, codegenTool / publishLocal).evaluated,
    ScriptedSettings.scriptedJdbcUrls,
    libraryDependencies ++= Seq(
      "org.scala-lang.modules" %% "scala-xml" % scalaXMLVersion,
      "org.scalatest" %% "scalatest-wordspec" % scalaTestVersion % Test
    ),
    buildInfoKeys := Seq[BuildInfoKey](
      "sbtJooqVersion" -> version.value,
      "javaxActivationVersion" -> javaxActivationVersion,
      "jaxbApiVersion" -> jaxbApiVersion,
      "jaxbCoreVersion" -> jaxbCoreVersion,
      "jaxbImplVersion" -> jaxbImplVersion,
      "javaxAnnotationApiVersion" -> javaxAnnotationApiVersion
    ),
    buildInfoPackage := "sbtjooq.codegen"
  )

lazy val codegenTool = project
  .in(file("codegen-tool"))
  .settings(
    name := "sbt-jooq-codegen-tool",
    crossPaths := false,
    autoScalaLibrary := false,
    libraryDependencies ++= Seq(
      "ch.qos.logback" % "logback-classic" % logbackVersion
    ),
    Compile / javacOptions ++= Seq("--release", "8")
  )

lazy val checker = project
  .dependsOn(core)
  .enablePlugins(SbtPlugin, BuildInfoPlugin)
  .settings(
    name := "sbt-jooq-checker",
    scripted := scripted.dependsOn(core / publishLocal, checkerTool / publishLocal).evaluated,
    addSbtPlugin("org.wartremover" % "sbt-wartremover" % wartRemoverVersion),
    buildInfoKeys := Seq[BuildInfoKey](
      "sbtJooqVersion" -> version.value
    ),
    buildInfoPackage := "sbtjooq.checker"
  )

lazy val checkerTool = project
  .in(file("checker-tool"))
  .settings(
    name := "sbt-jooq-checker-tool",
    scalaVersion := scala3,
    crossScalaVersions := Seq(scala3, scala213, scala212),
    libraryDependencies ++= Seq(
      "org.jooq" % "jooq" % jooqVersion,
      "org.wartremover" % "wartremover" % wartRemoverVersion cross CrossVersion.full,
      "org.scalatest" %% "scalatest-wordspec" % scalaTestVersion % Test
    ),
    scalacOptions ++= partialVersionSeq(scalaVersion.value) {
      case (2, _) => Seq("-Xsource:3")
    },
    Test / scalacOptions ++= partialVersionSeq(scalaVersion.value) {
      case (2, _) => Seq("-Xlint:-unused")
    }
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
      "H2_VERSION" -> h2Version
    ),
    scalacOptions ++= Seq(
      "-Xsource:3",
      "-Wconf:cat=unused-nowarn:s"
    ),
    libraryDependencies += sbtDependency.value,
    libraryDependencySchemes ++= Seq(
      "org.scala-lang.modules" %% "scala-xml" % "always"
    )
  )

lazy val versions = project
  .in(file(".versions"))
  .settings(
    publish / skip := true,
    scalaVersion := scala3,
    libraryDependencies ++= Seq(scala213, scala212).map("org.scala-lang" % "scala-library" % _),
    libraryDependencies ++= jooqVersions.map("org.jooq" % "jooq" % _),
    libraryDependencies ++= Seq(
      "com.h2database" % "h2" % h2Version
    )
  )
