ThisBuild / scalaVersion := sys.props("scala.version")

enablePlugins(JooqCodegenPlugin)

jooqVersion := sys.props("jooq.version")

jooqCodegenConfig :=
  <configuration>
    <jdbc>
      <driver>org.h2.Driver</driver>
      <url>jdbc:h2:./test</url>
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

libraryDependencies ++= Seq(Runtime, JooqCodegen).map { conf =>
  "com.h2database" % "h2" % sys.props("h2.version") % conf
}
