ThisBuild / scalaVersion := sys.props("scripted.scala.version")

enablePlugins(JooqCodegenPlugin)

inConfig(Test)(JooqCodegenPlugin.jooqCodegenSettings)

Compile / jooqCodegen / skip := true

jooqVersion := sys.props("scripted.jooq.version")

jooqCodegenConfig := file("jooq-codegen.xml")

libraryDependencies ++= Seq(Runtime, JooqCodegen).map { conf =>
  "com.h2database" % "h2" % sys.props("scripted.h2.version") % conf
}
