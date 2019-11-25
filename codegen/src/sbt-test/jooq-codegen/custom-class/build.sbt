scalaVersion in ThisBuild := "2.13.1"

enablePlugins(JooqCodegenPlugin)

jooqVersion := "3.12.3"

JooqCodegen / jooqModules += "jooq-meta-extensions"

jooqCodegenConfig := "classpath:jooq-codegen.xml"
