ThisBuild / scalaVersion := sys.props("scripted.scala.version")

lazy val core = project
  .enablePlugins(JooqCodegenPlugin)
  .settings(
    jooqVersion := sys.props("scripted.jooq.version"),
    JooqCodegen / jooqModules += "jooq-meta-extensions",
    jooqCodegenConfig := uri("classpath:jooq-codegen.xml"),
  )
