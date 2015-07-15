scalaVersion := "2.11.7"

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

javaOptions in jooq += "-Dfile.encoding=utf8"

Seq("runtime", "jooq").map { conf =>
  libraryDependencies += "com.h2database" % "h2" % "1.4.187" % conf
}
libraryDependencies ++= Seq(
  "org.jooq" % "jooq" % jooqVersion.value,
  "org.slf4j" % "slf4j-simple" % "1.7.12" % "runtime"
)

fork in run := true
javaOptions in run += "-Dorg.slf4j.simpleLogger.logFile=System.out"
