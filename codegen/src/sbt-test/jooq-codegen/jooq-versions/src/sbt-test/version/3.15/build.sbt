ThisBuild / scalaVersion := sys.props("scripted.scala.version")

ThisBuild / javacOptions ++= Seq("--release", "11")

enablePlugins(JooqCodegenPlugin)

jooqVersion := sys.props("scripted.jooq.3_15.version")

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
