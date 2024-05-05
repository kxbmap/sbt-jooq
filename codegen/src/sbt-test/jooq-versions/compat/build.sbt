enablePlugins(SbtPlugin)

publish / skip := true

scriptedBatchExecution := insideCI.value
scriptedParallelInstances := 2
scriptedBufferLog := insideCI.value

scriptedLaunchOpts ++=
  sys.props.collect {
    case (k, v) if k.startsWith("scripted.") => s"-D$k=$v"
  }.toSeq

TaskKey[Unit]("disableUnsupportedVersionTests") := {
  val java = sys.props("java.version").stripPrefix("1.").takeWhile(_.isDigit).toInt
  Seq(
    11 -> ">=3.15",
    17 -> ">=3.17"
  ).collectFirst {
    case (v, s) if java < v => SemanticSelector(s)
  }.foreach { s =>
    val dir = sbtTestDirectory.value / "version"
    val disables: NameFilter = n => s.matches(VersionNumber(n))
    IO.touch(IO.listFiles(dir, DirectoryFilter && disables).map(_ / "disabled"))
  }
}
