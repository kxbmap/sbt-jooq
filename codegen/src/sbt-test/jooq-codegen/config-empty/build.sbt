ThisBuild / scalaVersion := sys.props("scripted.scala.version")

enablePlugins(JooqCodegenPlugin)

jooqVersion := sys.props("scripted.jooq.version")
