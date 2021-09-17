ThisBuild / scalaVersion := sys.props("scala.version")

ThisBuild / javacOptions ++= Seq("-source", "8", "-target", "8")

enablePlugins(JooqCodegenPlugin)

jooqVersion := sys.props("jooq.3_12.version")

JooqCodegen / jooqModules += "jooq-meta-extensions"

jooqCodegenConfig := uri("classpath:jooq-codegen.xml")

Seq(JooqCodegen, Compile).flatMap { c => Seq(
  c / run / fork := true,
  c / run / javaHome := sys.env.get("RUNTIME_JAVA_HOME").map(file))
}
