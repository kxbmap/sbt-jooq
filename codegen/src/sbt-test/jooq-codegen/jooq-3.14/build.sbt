scalaVersion in ThisBuild := sys.props("scala.version")

enablePlugins(JooqCodegenPlugin)

jooqVersion := sys.props("jooq.3_14.version")

JooqCodegen / jooqModules += "jooq-meta-extensions"

jooqCodegenConfig := uri("classpath:jooq-codegen.xml")
