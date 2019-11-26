scalaVersion in ThisBuild := "2.13.1"

enablePlugins(JooqCodegenPlugin)

inConfig(Compile)(Seq(
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
        <generate>
          <deprecated>false</deprecated>
        </generate>
        <target>
          <packageName>com.example.db</packageName>
          <directory>{sourceManaged.value}</directory>
        </target>
      </generator>
    </configuration>
))

libraryDependencies ++= Seq(Runtime, JooqCodegen).map { conf =>
  "com.h2database" % "h2" % "1.4.196" % conf
}
