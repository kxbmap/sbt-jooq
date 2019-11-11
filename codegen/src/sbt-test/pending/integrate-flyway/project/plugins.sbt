lazy val pv = sys.props.get("plugin.version").getOrElse {
  sys.error(
    """The system property 'plugin.version' is not defined.
      |Specify this property using the scriptedLaunchOpts -D.""".stripMargin)
}
addSbtPlugin("com.github.kxbmap" % "sbt-jooq-codegen" % pv)

addSbtPlugin("org.flywaydb" % "flyway-sbt" % "4.2.0")

resolvers += "Flyway" at "https://flywaydb.org/repo"
