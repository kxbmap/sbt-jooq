ThisBuild / scalaVersion := sys.props("scala.version")

ThisBuild / javacOptions ++= Seq("--release", "11")

enablePlugins(JooqCodegenPlugin)

jooqVersion := sys.props("jooq.3_15.version")

JooqCodegen / jooqModules += "jooq-meta-extensions"

jooqCodegenConfig := uri("classpath:jooq-codegen.xml")

Seq(JooqCodegen, Compile).flatMap { c => Seq(
  c / run / fork := true,
  c / run / javaHome := sys.env.get("RUNTIME_JAVA_HOME").map(file))
}
