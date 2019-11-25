ThisBuild / scalaVersion := "2.13.1"

enablePlugins(JooqCodegenPlugin)

Compile / run / description := "Compile/run"
Test / run / description := "Test/run"

lazy val subProj = project.settings(
  Compile / run / description := "subProj/Compile/run",
  Test / run / description := "subProj/Test/run"
)

// Set settings
Compile / jooqCodegenKeys := Seq(
  Compile / run / description,
  Test / run / description,
  subProj / Compile / run / description,
  subProj / Test / run / description
)

addJooqCodegenSettingsTo(Test)

// Append settings
Test / jooqCodegenKeys ++= Seq(
  Compile / run / description,
  Test / run / description,
  subProj / Compile / run / description,
  subProj / Test / run / description
)


lazy val myTask = taskKey[String]("")
myTask := s"myTask: ${math.random()}"

Compile / jooqCodegenKeys += CodegenKey(myTask)    // Append task
Compile / jooqCodegenKeys += baseDirectory         // Append setting
Compile / jooqCodegenKeys += "Answer" -> 42        // Append constant
Compile / jooqCodegenKeys ++= Map("foo" -> "bar")  // Append Map of constants
Compile / jooqCodegenKeys ++= Seq[CodegenKey](     // Append mixture values
  "hoge" -> "piyo",
  myTask,
  Compile / sourceManaged,
  LocalRootProject / baseDirectory,
  CodegenKey(baseDirectory).map(_ / "project").withName("PROJECT_DIR")
)


TaskKey[Unit]("check") := {
  def check(msg: String, actual: Map[String, String], expected: Map[String, String]): Unit = {
    if (actual != expected) {
      println(s"Actual: $actual")
      println(s"Expected: $expected")
      println(s"A - E: ${actual.toSeq.diff(expected.toSeq)}")
      println(s"E - A: ${expected.toSeq.diff(actual.toSeq)}")
      sys.error(msg)
    }
  }

  check(
    "Check variables in Compile",
    (Compile / jooqCodegenConfigVariables).value,
    Map(
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
      "sourceManaged" -> (Compile / sourceManaged).value.toString,
      "compile:sourceManaged" -> (Compile / sourceManaged).value.toString,
      "LocalRootProject/baseDirectory" -> baseDirectory.value.toString,
      "PROJECT_DIR" -> (baseDirectory.value / "project").toString
    ) ++ sys.env)

  check(
    "Check variables in Test",
    (Test / jooqCodegenConfigVariables).value,
    Map(
      "baseDirectory" -> baseDirectory.value.toString,
      "sourceManaged" -> (Test / sourceManaged).value.toString,
      "test:sourceManaged" -> (Test / sourceManaged).value.toString,
      "jooq-codegen:resourceDirectory" -> (JooqCodegen / resourceDirectory).value.toString,

      "compile:run::description" -> "Compile/run",

      "run::description" -> "Test/run",
      "test:run::description" -> "Test/run",

      "subProj/compile:run::description" -> "subProj/Compile/run",

      "subProj/run::description" -> "subProj/Test/run",
      "subProj/test:run::description" -> "subProj/Test/run"
    ) ++ sys.env)

}
