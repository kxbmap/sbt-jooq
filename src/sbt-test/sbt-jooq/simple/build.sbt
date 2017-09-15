scalaVersion in ThisBuild := "2.12.3"

enablePlugins(JooqCodegen)

jooqCodegenConfigFile := Some(file("jooq-codegen.xml"))

libraryDependencies ++= Seq("runtime", "jooq").map { conf =>
  "com.h2database" % "h2" % "1.4.196" % conf
}
