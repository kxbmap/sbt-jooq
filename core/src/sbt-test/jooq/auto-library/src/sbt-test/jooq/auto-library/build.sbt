ThisBuild / scalaVersion := sys.props("scripted.scala.version")

enablePlugins(JooqPlugin)

jooqVersion := sys.props("scripted.jooq.version")
