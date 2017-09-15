scalaVersion in ThisBuild := "2.12.3"

enablePlugins(JooqCodegen)

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
      <generate>
        <deprecated>false</deprecated>
      </generate>
      <target>
        <packageName>com.example.db</packageName>
        <directory>{jooqCodegenTargetDirectory.value}</directory>
      </target>
    </generator>
  </configuration>

libraryDependencies ++= Seq("runtime", "jooq").map { conf =>
  "com.h2database" % "h2" % "1.4.196" % conf
}
