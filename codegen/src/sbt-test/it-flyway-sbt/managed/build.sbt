ThisBuild / scalaVersion := sys.props("scripted.scala.version")

enablePlugins(JooqCodegenPlugin)

jooqCodegenMode := CodegenMode.Always

jooqVersion := sys.props("scripted.jooq.version")

Compile / jooqCodegen := (Compile / jooqCodegen).dependsOn(migration / flywayMigrate).value

jooqCodegenConfig := file("jooq-codegen.xml")

libraryDependencies += "com.h2database" % "h2" % sys.props("scripted.h2.version") % JooqCodegen

lazy val migration = project
  .enablePlugins(FlywayPlugin)
  .settings(
    flywayUrl := "jdbc:h2:./test",
    flywaySchemas := Seq("PUBLIC"),
    flywayLocations := Seq("classpath:db/migration"),
    libraryDependencies += "com.h2database" % "h2" % sys.props("scripted.h2.version") % Runtime
  )
