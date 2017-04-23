scalaVersion in ThisBuild := "2.12.2"

libraryDependencies += "com.h2database" % "h2" % "1.4.194" % "test"

lazy val root = project.in(file("."))
  .dependsOn(codegen % "test")

lazy val codegen = project
  .enablePlugins(JooqCodegen)
  .settings(
    jooqCodegenConfigFile := Some(file("jooq-codegen.xml")),
    libraryDependencies += "com.h2database" % "h2" % "1.4.194" % "jooq"
  )
