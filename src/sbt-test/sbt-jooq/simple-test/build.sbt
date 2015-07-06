scalaVersion := "2.11.7"

enablePlugins(JooqCodegen)

JooqCodegen.jooqCodegenSettingsIn(Test)
jooqCodegenConfigFile in Test := Some(file("jooq-codegen.xml"))

javaOptions in jooq += "-Dfile.encoding=utf8"

Seq("test", "jooq").map { conf =>
  libraryDependencies += "com.h2database" % "h2" % "1.4.187" % conf
}
libraryDependencies ++= Seq(
  "org.jooq" % "jooq" % jooqVersion.value % "test",
  "org.slf4j" % "slf4j-simple" % "1.7.12" % "test"
)

fork in run := true
javaOptions in run += "-Dorg.slf4j.simpleLogger.logFile=System.out"
