scalaVersion in ThisBuild := "2.11.7"

libraryDependencies += "com.h2database" % "h2" % "1.4.191" % "test"

lazy val root = project.in(file("."))
  .dependsOn(codegen % "test")

lazy val codegen = project
  .enablePlugins(JooqCodegen)
  .settings(
    jooqCodegenConfigFile := Some(file("jooq-codegen.xml")),
    libraryDependencies += "com.h2database" % "h2" % "1.4.191" % "jooq"
  )
