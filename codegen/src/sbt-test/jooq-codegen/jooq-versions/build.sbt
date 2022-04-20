enablePlugins(SbtPlugin)

scriptedBufferLog := false

scriptedLaunchOpts ++=
  sys.props.collect {
    case (k, v) if k.startsWith("scripted.") => s"-D$k=$v"
  }.toSeq

scriptedLaunchOpts += {
  val setup = (baseDirectory.value / "sql" / "setup.sql").toString.replace('\\', '/')
  s"-Dscripted.jdbc.url=jdbc:h2:mem:;INIT=runscript from '$setup'"
}

TaskKey[Unit]("cleanup") := {
  val dir = ivyPaths.value.ivyHome.map(_ / "local" / organization.value)
  IO.deleteFilesEmptyDirs(dir)
}

TaskKey[Unit]("disableIncompatibleTests") := {
  val java = sys.props("java.version").stripPrefix("1.").takeWhile(_.isDigit).toInt
  if (java < 11) {
    val dir = sbtTestDirectory.value / "version"
    val disables: NameFilter = {
      val s = SemanticSelector(">=3.15")
      n => s.matches(VersionNumber(n))
    }
    IO.touch(IO.listFiles(dir, DirectoryFilter && disables).map(_ / "disabled"))
  }
}
