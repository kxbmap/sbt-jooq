ThisBuild / scalaVersion := sys.props("scripted.scala.version")

enablePlugins(JooqCheckerPlugin)

jooqVersion := sys.props("scripted.jooq.version")

inConfig(Test)(inTask(compile)(JooqCheckerPlugin.jooqCheckerSettings))
