ThisBuild / scalaVersion := sys.props("scripted.scala.version")

enablePlugins(JooqCodegenPlugin)

jooqVersion := sys.props("scripted.jooq.version")

def configXML(db: String) =
  <configuration>
    <jdbc>
      <driver>org.h2.Driver</driver>
      <url>jdbc:h2:./{db}</url>
    </jdbc>
    <generator>
      <database>
        <name>org.jooq.meta.h2.H2Database</name>
        <includes>.*</includes>
        <inputSchema>PUBLIC</inputSchema>
      </database>
      <target>
        <packageName>com.example.{db}</packageName>
      </target>
    </generator>
  </configuration>

jooqCodegenConfig ++= Seq(configXML("db0"), configXML("db1"))
