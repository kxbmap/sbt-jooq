ThisBuild / scalaVersion := sys.props("scala.version")

enablePlugins(JooqCodegenPlugin)

jooqVersion := sys.props("jooq.version")

jooqCodegenConfig := file("jooq-codegen.xml")

libraryDependencies ++= Seq(Runtime, JooqCodegen).map { conf =>
  "com.h2database" % "h2" % sys.props("h2.version") % conf
}
