import com.github.kxbmap.sbt.jooq.CodegenUtil

scalaVersion in ThisBuild := "2.12.4"

enablePlugins(JooqCodegen)

libraryDependencies ++= Seq("runtime", "jooq").map { conf =>
  "com.h2database" % "h2" % "1.4.196" % conf
}

val myTargetDirs = Def.setting(Seq(
  baseDirectory.value / "generated" / "db1",
  baseDirectory.value / "generated" / "db2"
))

managedSourceDirectories in Compile ++= myTargetDirs.value

jooqCodegenGeneratedSourcesFinder := myTargetDirs.value ** "*.java"

jooqCodegen := Def.sequential(
  (run in Jooq).toTask(" config/db1.xml config/db2.xml"),
  Def.task(jooqCodegenGeneratedSourcesFinder.value.get)
).value

// to delete generated files on clean
cleanFiles ++= myTargetDirs.value
