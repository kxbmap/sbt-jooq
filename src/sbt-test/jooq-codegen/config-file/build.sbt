

enablePlugins(JooqCodegenPlugin)

jooqCodegenConfig := file("jooq-codegen.xml")

libraryDependencies ++= Seq("runtime", "jooq-codegen").map { conf =>
  "com.h2database" % "h2" % "1.4.196" % conf
}
