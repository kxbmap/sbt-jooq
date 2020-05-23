addSbtPlugin("com.github.kxbmap" % "sbt-jooq-codegen" % sys.props("plugin.version"))

addSbtPlugin("org.flywaydb" % "flyway-sbt" % "4.2.0")

resolvers += "Flyway" at "https://flywaydb.org/repo"
