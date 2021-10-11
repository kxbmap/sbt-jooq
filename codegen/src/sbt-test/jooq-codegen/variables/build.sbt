ThisBuild / scalaVersion := sys.props("scala.version")

enablePlugins(JooqCodegenPlugin)

jooqVersion := sys.props("jooq.version")

JooqCodegen / jooqModules += "jooq-meta-extensions"

jooqCodegenConfig := uri("classpath:jooq-codegen.xml")

jooqCodegenVariables ++= Map(
  "PACKAGE" -> "com.example.db",
  "DATABASE_NAME" -> "org.jooq.meta.extensions.ddl.DDLDatabase",
  "DATABASE_PROPS" -> {
    val props = new java.util.Properties()
    props.setProperty("scripts", ((JooqCodegen / resourceDirectory).value / "database.sql").toString)
    props
  }
)
