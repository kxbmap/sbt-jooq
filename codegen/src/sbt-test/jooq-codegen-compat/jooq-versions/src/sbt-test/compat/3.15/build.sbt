ThisBuild / scalaVersion := sys.props("scala.version")

ThisBuild / javacOptions ++= Seq("--release", "11")

enablePlugins(JooqCodegenPlugin)

jooqVersion := sys.props("jooq.3_15.version")

JooqCodegen / jooqModules += "jooq-meta-extensions"

jooqCodegenConfig := uri("classpath:jooq-codegen.xml")

Seq(JooqCodegen, Compile).flatMap(c => Seq(
  c / run / fork := true,
  c / run / javaHome := {
    val key = "RUNTIME_JAVA_HOME"
    (if (insideCI.value) Some(sys.env(key)) else sys.env.get(key)).map(file)
  }))
