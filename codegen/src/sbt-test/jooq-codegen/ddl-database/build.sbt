scalaVersion in ThisBuild := "2.13.1"

enablePlugins(JooqCodegenPlugin)

JooqCodegen / jooqModules += "jooq-meta-extensions"

jooqCodegenConfig := "classpath:jooq-codegen.xml"
