scalaVersion in ThisBuild := "2.12.7"

enablePlugins(JooqCodegenPlugin)

description in (Compile, run) := "Compile/run"
description in (Test, run) := "Test/run"

lazy val subProj = project.settings(
  description in (Compile, run) := "subProj/Compile/run",
  description in (Test, run) := "subProj/Test/run"
)

// Set settings
jooqCodegenKeys in Compile := Seq(
  description in (Compile, run),
  description in (Test, run),
  description in (subProj, Compile, run),
  description in (subProj, Test, run)
)

addJooqCodegenSettingsTo(Test)

// Append settings
jooqCodegenKeys in Test ++= Seq(
  description in (Compile, run),
  description in (Test, run),
  description in (subProj, Compile, run),
  description in (subProj, Test, run)
)


lazy val myTask = taskKey[String]("")
myTask := s"myTask: ${math.random()}"

jooqCodegenKeys in Compile += CodegenKey(myTask)    // Append task
jooqCodegenKeys in Compile += baseDirectory         // Append setting
jooqCodegenKeys in Compile += "Answer" -> 42        // Append constant
jooqCodegenKeys in Compile ++= Map("foo" -> "bar")  // Append Map of constants
jooqCodegenKeys in Compile ++= Seq[CodegenKey](     // Append mixture values
  "hoge" -> "piyo",
  myTask,
  sourceManaged in Compile,
  baseDirectory in LocalRootProject,
  CodegenKey(baseDirectory).map(_ / "project").withName("PROJECT_DIR")
)


TaskKey[Unit]("check") := {
  def check(msg: String, actual: Seq[(String, String)], expected: Map[String, String]): Unit = {
    if (actual.toMap != expected) {
      println(s"Actual: $actual")
      println(s"Expected: $expected")
      println(s"A - E: ${actual.diff(expected.toSeq)}")
      println(s"E - A: ${expected.toSeq.diff(actual)}")
      sys.error(msg)
    }
  }

  check("Check values in Compile", (jooqCodegenSubstitutions in Compile).value, Map(
    "run::description" -> "Compile/run",
    "compile:run::description" -> "Compile/run",

    "test:run::description" -> "Test/run",

    "subProj/run::description" -> "subProj/Compile/run",
    "subProj/compile:run::description" -> "subProj/Compile/run",

    "subProj/test:run::description" -> "subProj/Test/run",

    "myTask" -> myTask.value.toString,
    "baseDirectory" -> baseDirectory.value.toString,
    "Answer" -> "42",
    "foo" -> "bar",
    "hoge" -> "piyo",
    "sourceManaged" -> (sourceManaged in Compile).value.toString,
    "compile:sourceManaged" -> (sourceManaged in Compile).value.toString,
    "LocalRootProject/baseDirectory" -> baseDirectory.value.toString,
    "PROJECT_DIR" -> (baseDirectory.value / "project").toString
  ))

  check("Check values in Test", (jooqCodegenSubstitutions in Test).value, sys.env ++ Map(
    "baseDirectory" -> baseDirectory.value.toString,
    "sourceManaged" -> (sourceManaged in Test).value.toString,
    "test:sourceManaged" -> (sourceManaged in Test).value.toString
  ) ++ Map(
    "compile:run::description" -> "Compile/run",

    "run::description" -> "Test/run",
    "test:run::description" -> "Test/run",

    "subProj/compile:run::description" -> "subProj/Compile/run",

    "subProj/run::description" -> "subProj/Test/run",
    "subProj/test:run::description" -> "subProj/Test/run"
  ))

}
