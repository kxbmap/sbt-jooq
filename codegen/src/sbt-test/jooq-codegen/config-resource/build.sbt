scalaVersion in ThisBuild := sys.props("scala.version")

enablePlugins(JooqCodegenPlugin)

jooqVersion := sys.props("jooq.version")

jooqCodegenConfig := uri("classpath:jooq-codegen.xml")

libraryDependencies ++= Seq(Runtime, JooqCodegen).map { conf =>
  "com.h2database" % "h2" % sys.props("h2.version") % conf
}
