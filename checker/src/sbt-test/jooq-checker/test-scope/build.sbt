ThisBuild / scalaVersion := sys.props("scala.version")

enablePlugins(JooqCheckerPlugin)

jooqVersion := sys.props("jooq.version")

addJooqCheckerSettingsTo(Test, compile)
