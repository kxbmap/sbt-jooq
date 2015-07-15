scalaVersion in ThisBuild := "2.11.7"

lazy val root = project.in(file("."))
  .dependsOn(codegen % "test")

lazy val codegen = project
  .enablePlugins(JooqCodegen)
  .settings(
    jooqCodegenConfigFile := Some(file("jooq-codegen.xml")),
    javaOptions in jooq += "-Dfile.encoding=utf8",
    libraryDependencies ++= Seq(
      "org.jooq" % "jooq" % jooqVersion.value,
      "com.h2database" % "h2" % "1.4.187" % "jooq"
    )
  )

libraryDependencies ++= Seq(
  "org.slf4j" % "slf4j-simple" % "1.7.12" % "test",
  "com.h2database" % "h2" % "1.4.187" % "test"
)

fork in run := true
javaOptions in run += "-Dorg.slf4j.simpleLogger.logFile=System.out"
