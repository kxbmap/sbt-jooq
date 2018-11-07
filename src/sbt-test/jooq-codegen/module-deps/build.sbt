scalaVersion in ThisBuild := "2.12.7"

lazy val upstream = project
  .enablePlugins(JooqPlugin)
  .settings(
    jooqModules += "jooq-codegen"
  )

lazy val app = project
  .enablePlugins(JooqCodegenPlugin)
  .settings(
    libraryDependencies ++= Seq("runtime", "jooq-codegen").map { conf =>
      "com.h2database" % "h2" % "1.4.196" % conf
    },
    inConfig(Compile)(Seq(
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
  )
  .dependsOn(upstream % "jooq-codegen")
