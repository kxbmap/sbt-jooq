scalaVersion in ThisBuild := "2.13.1"

enablePlugins(JooqCheckerPlugin)

jooqVersion := sys.props("jooq.version")

addJooqCheckerSettingsTo(Test, compile)
