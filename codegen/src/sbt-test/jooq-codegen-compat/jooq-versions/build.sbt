enablePlugins(SbtPlugin)

scriptedBufferLog := false

scriptedLaunchOpts ++=
  sys.props.collect {
    case (k, v) if k.endsWith(".version") => s"-D$k=$v"
  }.toSeq

lazy val copyChanges = taskKey[Unit]("")
lazy val disableIncompatibleTests = taskKey[Unit]("")

scripted := scripted.dependsOn(copyChanges, disableIncompatibleTests).evaluated

copyChanges := {
  val changes = file("changes")
  val testDir = sbtTestDirectory.value / "compat"
  val copies = for {
    (f, d) <- Seq(
      "test" -> ".",
      "plugins.sbt" -> "project",
      "Main.scala" -> "src/main/scala/com/example",
      "database.sql" -> "src/jooq-codegen/resources",
    )
    ver <- (10 to 15).map(v => s"3.$v")
  } yield
    (changes / f) -> (testDir / ver / d / f)

  IO.copy(copies)
}

disableIncompatibleTests := {
  val key = "RUNTIME_JAVA_HOME"
  val home = if (insideCI.value) Some(sys.env(key)) else sys.env.get(key)
  val version = home.map(
    file(_).getName.dropWhile(!_.isDigit).stripPrefix("1.").takeWhile(_.isDigit).toInt)

  if (version.exists(_ < 11))
    IO.touch(sbtTestDirectory.value / "compat" / "3.15" / "disabled")
}
