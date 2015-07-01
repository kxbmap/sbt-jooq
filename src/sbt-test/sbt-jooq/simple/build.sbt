scalaVersion := "2.11.7"

enablePlugins(JooqPlugin)

jooqConfigFile := file("jooq-codegen.xml")

javaOptions in jooq += "-Dfile.encoding=utf8"

Seq("compile", "jooq").map { conf =>
  libraryDependencies += "com.h2database" % "h2" % "1.4.187" % conf
}
libraryDependencies += "org.slf4j" % "slf4j-simple" % "1.7.12"

fork in run := true
javaOptions in run += "-Dorg.slf4j.simpleLogger.logFile=System.out"
