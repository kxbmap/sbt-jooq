scalaVersion in ThisBuild := "2.13.1"

enablePlugins(JooqCodegenPlugin)

jooqCodegen := jooqCodegen.dependsOn(flywayMigrate in migration).value

jooqCodegenConfig := file("jooq-codegen.xml")

jooqCodegenStrategy := CodegenStrategy.Always

libraryDependencies ++= Seq(Runtime, JooqCodegen).map { conf =>
  "com.h2database" % "h2" % "1.4.200" % conf
}

lazy val migration = project.settings(
  flywayUrl := "jdbc:h2:./test",
  flywaySchemas := Seq("PUBLIC"),
  flywayLocations := Seq("classpath:db/migration"),
  libraryDependencies += "com.h2database" % "h2" % "1.4.200" % Runtime
)
