scalaVersion := "2.11.7"

enablePlugins(JooqCodegen)

jooqCodegenConfigFile := Some(file("jooq-codegen.xml"))

javaOptions in jooq += "-Dfile.encoding=utf8"

Seq("runtime", "jooq").map { conf =>
  libraryDependencies += "com.h2database" % "h2" % "1.4.187" % conf
}
libraryDependencies ++= Seq(
  "org.jooq" % "jooq" % jooqVersion.value,
  "org.slf4j" % "slf4j-simple" % "1.7.12" % "runtime"
)

fork in run := true
javaOptions in run += "-Dorg.slf4j.simpleLogger.logFile=System.out"
