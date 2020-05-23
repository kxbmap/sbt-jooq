scalaVersion in ThisBuild := "2.13.1"

enablePlugins(JooqCodegenPlugin)

jooqVersion := sys.props("jooq.version")

JooqCodegen / jooqModules += "jooq-meta-extensions"

jooqCodegenConfig := uri("classpath:jooq-codegen.xml")
