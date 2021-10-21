ThisBuild / scalaVersion := sys.props("scripted.scala.version")

ThisBuild / javacOptions ++= Seq("-source", "8", "-target", "8")

enablePlugins(JooqCodegenPlugin)

jooqVersion := sys.props("scripted.jooq.3_10.version")

jooqCodegenConfig :=
  <configuration>
    <jdbc>
      <driver>org.h2.Driver</driver>
      <url>jdbc:h2:./test</url>
    </jdbc>
    <generator>
      <database>
        <name>org.jooq.util.h2.H2Database</name>
        <includes>.*</includes>
        <inputSchema>PUBLIC</inputSchema>
      </database>
      <target>
        <packageName>com.example.db</packageName>
      </target>
    </generator>
  </configuration>
