enablePlugins(SbtPlugin)

scriptedBufferLog := false

scriptedLaunchOpts ++=
  Seq("plugin", "scala", "jooq.3_15")
    .map(x => s"$x.version")
    .map(x => s"-D$x=${sys.props(x)}")

scripted := scripted.dependsOn(Def.task {
  if (
    sys.env.get("RUNTIME_JAVA_HOME")
      .map(file(_).getName)
      .map(_.dropWhile(!_.isDigit).takeWhile(_.isDigit).toInt)
      .exists(_ < 11)
  ) IO.touch(sbtTestDirectory.value / "compat" / "3.15" / "disabled")
}).evaluated
