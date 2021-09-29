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
  val dir = sbtTestDirectory.value / "compat"
  val copies = for {
    (src, dst) <- Seq(
      "test" -> "test",
      "plugins.sbt" -> "project/plugins.sbt",
      "Main.scala" -> "src/main/scala/com/example/Main.scala",
      "database.sql" -> "src/jooq-codegen/resources/database.sql",
    )
    ver <- 10 to 15
  } yield
    (file("changes") / src) -> (dir / s"3.$ver" / dst)

  IO.copy(copies)
}

disableIncompatibleTests := {
  val key = "RUNTIME_JAVA_HOME"
  val home = if (insideCI.value) Some(sys.env(key)) else sys.env.get(key)
  val version = home
    .map(file(_).getName.dropWhile(!_.isDigit))
    .map(s => if (s.startsWith("1.")) s.drop(2) else s)
    .map(_.takeWhile(_.isDigit).toInt)

  if (version.exists(_ < 11))
    IO.touch(sbtTestDirectory.value / "compat" / "3.15" / "disabled")
}
