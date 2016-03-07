scalaVersion in ThisBuild := "2.11.7"

enablePlugins(JooqCodegen)

jooqCodegen <<= jooqCodegen.dependsOn(flywayMigrate in migration)

jooqCodegenConfigFile := Some(file("jooq-codegen.xml"))

libraryDependencies ++= Seq("runtime", "jooq").map { conf =>
  "com.h2database" % "h2" % "1.4.191" % conf
}

lazy val migration = project.settings(
  flywayUrl := "jdbc:h2:./test",
  flywaySchemas := Seq("PUBLIC"),
  flywayLocations := Seq("classpath:db/migration"),
  libraryDependencies += "com.h2database" % "h2" % "1.4.191" % "runtime"
)
