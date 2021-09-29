enablePlugins(SbtPlugin)

scriptedBufferLog := false

scriptedLaunchOpts ++=
  Seq("plugin", "scala", "jooq.3_15")
    .map(x => s"$x.version")
    .map(x => s"-D$x=${sys.props(x)}")

scripted := scripted.dependsOn(Def.task {
  val key = "RUNTIME_JAVA_HOME"
  val home = if (insideCI.value) Some(sys.env(key)) else sys.env.get(key)
  val version = home.map(file(_).getName.dropWhile(!_.isDigit).takeWhile(_.isDigit).toInt)
  if (version.exists(_ < 11))
    IO.touch(sbtTestDirectory.value / "compat" / "3.15" / "disabled")
}).evaluated
