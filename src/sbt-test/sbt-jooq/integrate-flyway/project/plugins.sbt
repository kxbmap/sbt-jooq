lazy val pv = sys.props.get("plugin.version").getOrElse {
  sys.error(
    """The system property 'plugin.version' is not defined.
      |Specify this property using the scriptedLaunchOpts -D.""".stripMargin)
}
addSbtPlugin("com.github.kxbmap" % "sbt-jooq" % pv)

addSbtPlugin("org.flywaydb" % "flyway-sbt" % "3.2.1")

resolvers += "Flyway" at "http://flywaydb.org/repo"
