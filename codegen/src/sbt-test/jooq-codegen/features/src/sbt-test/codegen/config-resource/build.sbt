ThisBuild / scalaVersion := sys.props("scripted.scala.version")

libraryDependencies ++=
  Seq(Runtime, JooqCodegen).map { conf =>
    "com.h2database" % "h2" % sys.props("scripted.h2.version") % conf
  }

enablePlugins(JooqCodegenPlugin)

jooqVersion := sys.props("scripted.jooq.version")

jooqCodegenConfig := uri("classpath:jooq-codegen.xml")

jooqCodegenVariables += "JDBC_URL" -> sys.props("scripted.jdbc.url")
