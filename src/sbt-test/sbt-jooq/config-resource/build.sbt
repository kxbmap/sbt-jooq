scalaVersion in ThisBuild := "2.12.6"

enablePlugins(JooqCodegen)

jooqCodegenConfig := resource"/jooq-codegen.xml"

libraryDependencies ++= Seq("runtime", "jooq").map { conf =>
  "com.h2database" % "h2" % "1.4.196" % conf
}
