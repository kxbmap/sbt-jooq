scalaVersion in ThisBuild := "2.13.1"

enablePlugins(JooqCheckerPlugin)

addJooqCheckerSettingsTo(Test, compile)
