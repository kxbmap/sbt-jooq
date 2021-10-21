ThisBuild / scalaVersion := sys.props("scripted.scala.version")

enablePlugins(JooqCodegenPlugin)

jooqVersion := sys.props("scripted.jooq.version")

jooqCodegenVariables ++= Map(
  "JDBC_DRIVER" -> "org.h2.Driver",
  "JDBC_URL" -> "jdbc:h2:./test",
  "JDBC_PROPS" -> {
    val props = new java.util.Properties()
    props.setProperty("user", "")
    props.setProperty("password", "")
    props
  },
  "DATABASE_NAME" -> "org.jooq.meta.h2.H2Database",
  "INPUT_SCHEMA" -> "PUBLIC",
  "PACKAGE" -> "com.example.db",
)

jooqCodegenConfig :=
  <configuration>
    <jdbc>
      <driver>${{JDBC_DRIVER}}</driver>
      <url>${{JDBC_URL}}</url>
      <properties>${{JDBC_PROPS}}</properties>
    </jdbc>
    <generator>
      <database>
        <name>${{DATABASE_NAME}}</name>
        <includes>.*</includes>
        <inputSchema>${{INPUT_SCHEMA}}</inputSchema>
      </database>
      <target>
        <packageName>${{PACKAGE}}</packageName>
      </target>
    </generator>
  </configuration>
