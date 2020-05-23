scalaVersion in ThisBuild := sys.props("scala.version")

enablePlugins(JooqCodegenPlugin)

jooqVersion := sys.props("jooq.version")

jooqCodegen := jooqCodegen.dependsOn(flywayMigrate in migration).value

jooqCodegenConfig := file("jooq-codegen.xml")

jooqCodegenStrategy := CodegenStrategy.Always

libraryDependencies ++= Seq(Runtime, JooqCodegen).map { conf =>
  "com.h2database" % "h2" % sys.props("h2.version") % conf
}

lazy val migration = project.settings(
  flywayUrl := "jdbc:h2:./test",
  flywaySchemas := Seq("PUBLIC"),
  flywayLocations := Seq("classpath:db/migration"),
  libraryDependencies += "com.h2database" % "h2" % sys.props("h2.version") % Runtime
)
