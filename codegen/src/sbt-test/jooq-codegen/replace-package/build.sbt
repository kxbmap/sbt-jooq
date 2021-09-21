ThisBuild / scalaVersion := sys.props("scala.version")

lazy val core = project
  .enablePlugins(JooqCodegenPlugin)
  .settings(
    jooqVersion := sys.props("jooq.version"),
    JooqCodegen / jooqModules += "jooq-meta-extensions",
    jooqCodegenConfig := uri("classpath:jooq-codegen.xml")
  )
