scalaVersion in ThisBuild := "2.12.2"

lazy val upstream = project
  .settings(
    libraryDependencies ++= Seq(
      "org.jooq" % "jooq-codegen" % JooqCodegen.DefaultJooqVersion
    )
  )

lazy val app = project
  .enablePlugins(JooqCodegen)
  .settings(
    libraryDependencies ++= Seq("runtime", "jooq").map { conf =>
      "com.h2database" % "h2" % "1.4.191" % conf
    },
    jooqCodegenConfig :=
      <configuration>
        <jdbc>
          <driver>org.h2.Driver</driver>
          <url>jdbc:h2:../test</url>
        </jdbc>
        <generator>
          <strategy>
            <name>com.example.CustomStrategy</name>
          </strategy>
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
  )
  .dependsOn(upstream % "jooq")
