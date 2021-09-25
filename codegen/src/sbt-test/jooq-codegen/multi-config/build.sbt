ThisBuild / scalaVersion := sys.props("scala.version")

enablePlugins(JooqCodegenPlugin)

jooqVersion := sys.props("jooq.version")

jooqCodegenConfig := Seq[CodegenConfig](
  file("jooq-codegen.xml"),
  uri("classpath:jooq-codegen.xml")
)

libraryDependencies ++= Seq(Runtime, JooqCodegen).map { conf =>
  "com.h2database" % "h2" % sys.props("h2.version") % conf
}
