sys.props.get("plugin.version").map { pluginVersion =>
  addSbtPlugin("com.github.kxbmap" % "sbt-jooq" % pluginVersion)
}.getOrElse {
  sys.error(
    """The system property 'plugin.version' is not defined.
      |Specify this property using the scriptedLaunchOpts -D.""".stripMargin)
}
