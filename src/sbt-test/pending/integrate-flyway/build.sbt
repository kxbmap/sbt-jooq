scalaVersion in ThisBuild := "2.12.6"

enablePlugins(JooqCodegenPlugin)

jooqCodegen := jooqCodegen.dependsOn(flywayMigrate in migration).value

jooqCodegenConfig := file("jooq-codegen.xml")

jooqCodegenStrategy := CodegenStrategy.Always

libraryDependencies ++= Seq("runtime", "jooq").map { conf =>
  "com.h2database" % "h2" % "1.4.196" % conf
}

lazy val migration = project.settings(
  flywayUrl := "jdbc:h2:./test",
  flywaySchemas := Seq("PUBLIC"),
  flywayLocations := Seq("classpath:db/migration"),
  libraryDependencies += "com.h2database" % "h2" % "1.4.196" % "runtime"
)
