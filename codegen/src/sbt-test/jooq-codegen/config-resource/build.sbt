scalaVersion in ThisBuild := "2.13.1"

enablePlugins(JooqCodegenPlugin)

jooqCodegenConfig := "classpath:jooq-codegen.xml"

libraryDependencies ++= Seq("runtime", "jooq-codegen").map { conf =>
  "com.h2database" % "h2" % "1.4.196" % conf
}
