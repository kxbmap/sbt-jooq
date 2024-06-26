ThisBuild / scalaVersion := sys.props("scripted.scala.version")

libraryDependencies ++=
  Seq(Runtime, JooqCodegen).map { conf =>
    "com.h2database" % "h2" % sys.props("scripted.h2.version") % conf
  }

enablePlugins(JooqCodegenPlugin)

jooqVersion := sys.props("scripted.jooq.3_19.version")

jooqCodegenConfig :=
  <configuration>
    <jdbc>
      <driver>org.h2.Driver</driver>
      <url>{sys.props("scripted.jdbc.url")}</url>
    </jdbc>
    <generator>
      <database>
        <name>org.jooq.meta.h2.H2Database</name>
        <includes>.*</includes>
        <inputSchema>PUBLIC</inputSchema>
      </database>
      <target>
        <packageName>com.example.db</packageName>
      </target>
    </generator>
  </configuration>
