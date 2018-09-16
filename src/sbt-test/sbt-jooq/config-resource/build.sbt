scalaVersion in ThisBuild := "2.12.6"

enablePlugins(JooqCodegenPlugin)

jooqCodegenConfig := "classpath:jooq-codegen.xml"

libraryDependencies ++= Seq("runtime", "jooq-codegen").map { conf =>
  "com.h2database" % "h2" % "1.4.196" % conf
}
