scalaVersion in ThisBuild := sys.props("scala.version")

enablePlugins(JooqCodegenPlugin)

jooqVersion := "3.12.4"

JooqCodegen / jooqModules += "jooq-meta-extensions"

jooqCodegenConfig := uri("classpath:jooq-codegen.xml")
