import Versions._

name := "sbt-jooq"

publish / skip := true

ThisBuild / scalacOptions ++= Seq(
  "-release", "8",
  "-deprecation",
  "-unchecked",
  "-feature",
  "-Xlint",
)

lazy val core = project
  .enablePlugins(SbtPlugin, BuildInfoPlugin)
  .settings(
    name := "sbt-jooq-core",
    buildInfoKeys := Seq[BuildInfoKey](
      "defaultJooqVersion" -> jooqVersion,
    ),
    buildInfoPackage := "sbtjooq",
  )

lazy val codegen = project
  .dependsOn(core)
  .enablePlugins(SbtPlugin, BuildInfoPlugin)
  .settings(
    name := "sbt-jooq-codegen",
    scripted := scripted.dependsOn(
      core / publishLocal,
      codegenTool / publishLocal,
    ).evaluated,
    libraryDependencies ++= Seq(
      "org.scala-lang.modules" %% "scala-xml" % scalaXMLVersion,
      "org.scalatest" %% "scalatest-wordspec" % scalaTestVersion % Test,
    ),
    buildInfoKeys := Seq[BuildInfoKey](
      "sbtJooqVersion" -> version.value,
      "javaxActivationVersion" -> javaxActivationVersion,
      "jaxbApiVersion" -> jaxbApiVersion,
      "jaxbCoreVersion" -> jaxbCoreVersion,
      "jaxbImplVersion" -> jaxbImplVersion,
      "javaxAnnotationApiVersion" -> javaxAnnotationApiVersion,
    ),
    buildInfoPackage := "sbtjooq.codegen",
  )

lazy val codegenTool = project
  .in(file("codegen-tool"))
  .settings(
    name := "sbt-jooq-codegen-tool",
    crossPaths := false,
    autoScalaLibrary := false,
    libraryDependencies ++= Seq(
      "ch.qos.logback" % "logback-classic" % logbackVersion,
    ),
    Compile / javacOptions ++= Seq("--release", "8"),
  )

lazy val checker = project
  .dependsOn(core)
  .enablePlugins(SbtPlugin, BuildInfoPlugin)
  .settings(
    name := "sbt-jooq-checker",
    scripted := scripted.dependsOn(core / publishLocal).evaluated,
    addSbtPlugin("org.wartremover" % "sbt-wartremover" % sbtWartRemoverVersion),
    buildInfoKeys := Seq[BuildInfoKey](
      "defaultJooqWartsVersion" -> jooqWartsVersion,
    ),
    buildInfoPackage := "sbtjooq.checker",
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
    libraryDependencies += sbtDependency.value,
    libraryDependencySchemes ++= Seq(
      "org.scala-lang.modules" %% "scala-xml" % "always",
    ),
  )

TaskKey[Unit]("updateReadme") := Def.taskDyn {
  val readmeFile = "README.md"
  Def.sequential(
    (docs / mdoc).toTask(s" --include $readmeFile"),
    Def.task(IO.copyFile((docs / mdocOut).value / readmeFile, baseDirectory.value / readmeFile)),
  )
}
