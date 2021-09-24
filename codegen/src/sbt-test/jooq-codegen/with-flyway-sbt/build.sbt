ThisBuild / scalaVersion := sys.props("scala.version")

enablePlugins(JooqCodegenPlugin)

jooqVersion := sys.props("jooq.version")

Compile / jooqCodegen := (Compile / jooqCodegen).dependsOn(migration / flywayMigrate).value

jooqCodegenConfig := file("jooq-codegen.xml")

jooqCodegenAutoStrategy := AutoStrategy.Always

libraryDependencies += "com.h2database" % "h2" % sys.props("h2.version") % JooqCodegen

lazy val migration = project
  .enablePlugins(FlywayPlugin)
  .settings(
    flywayUrl := "jdbc:h2:./test",
    flywaySchemas := Seq("PUBLIC"),
    flywayLocations := Seq("classpath:db/migration"),
    libraryDependencies += "com.h2database" % "h2" % sys.props("h2.version") % Runtime
  )
