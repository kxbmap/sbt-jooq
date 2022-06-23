enablePlugins(SbtPlugin)

scriptedLaunchOpts ++=
  sys.props.collect {
    case (k, v) if k.startsWith("scripted.") => s"-D$k=$v"
  }.toSeq

TaskKey[Unit]("cleanup") := {
  val dir = ivyPaths.value.ivyHome.map(_ / "local" / organization.value)
  IO.deleteFilesEmptyDirs(dir)
}

TaskKey[Unit]("disableIncompatibleTests") := {
  val java = sys.props("java.version").stripPrefix("1.").takeWhile(_.isDigit).toInt
  Seq(
    11 -> ">=3.15",
    17 -> ">=3.17",
  ).collectFirst {
    case (v, s) if java < v => SemanticSelector(s)
  }.foreach { s =>
    val dir = sbtTestDirectory.value / "version"
    val disables: NameFilter = n => s.matches(VersionNumber(n))
    IO.touch(IO.listFiles(dir, DirectoryFilter && disables).map(_ / "disabled"))
  }
}
