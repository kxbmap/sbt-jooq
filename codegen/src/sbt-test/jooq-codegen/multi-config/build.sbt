ThisBuild / scalaVersion := sys.props("scripted.scala.version")

libraryDependencies ++=
  Seq(Runtime, JooqCodegen).map { conf =>
    "com.h2database" % "h2" % sys.props("scripted.h2.version") % conf
  }

enablePlugins(JooqCodegenPlugin)

jooqVersion := sys.props("scripted.jooq.version")

def configXML(pkg: String, url: String) =
  <configuration>
    <jdbc>
      <driver>org.h2.Driver</driver>
      <url>{url}</url>
    </jdbc>
    <generator>
      <database>
        <name>org.jooq.meta.h2.H2Database</name>
        <includes>.*</includes>
        <inputSchema>PUBLIC</inputSchema>
      </database>
      <target>
        <packageName>com.example.{pkg}</packageName>
      </target>
    </generator>
  </configuration>

jooqCodegenConfig ++= Seq(
  configXML("db0", sys.props("scripted.jdbc.url.0")),
  configXML("db1", sys.props("scripted.jdbc.url.1"))
)
