ThisBuild / scalaVersion := sys.props("scripted.scala.version")

enablePlugins(JooqCodegenPlugin)

Compile / jooqCodegen / skip := true

jooqVersion := sys.props("scripted.jooq.version")

addJooqCodegenSettingsTo(Test)

Test / jooqCodegenConfig := file("jooq-codegen.xml")

libraryDependencies ++= Seq(Runtime, JooqCodegen).map { conf =>
  "com.h2database" % "h2" % sys.props("scripted.h2.version") % conf
}
