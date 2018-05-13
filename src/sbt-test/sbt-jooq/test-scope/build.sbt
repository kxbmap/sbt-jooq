scalaVersion in ThisBuild := "2.12.6"

enablePlugins(JooqCodegen)

addJooqCodegenSettingsTo(Test)

jooqCodegenConfig in Test := file("jooq-codegen.xml")

libraryDependencies ++= Seq("test", "jooq").map { conf =>
  "com.h2database" % "h2" % "1.4.196" % conf
}
