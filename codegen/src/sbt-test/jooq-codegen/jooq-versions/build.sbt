enablePlugins(SbtPlugin)

scriptedBufferLog := false

scriptedLaunchOpts ++=
  sys.props.collect {
    case (k, v) if k.startsWith("scripted.") => s"-D$k=$v"
  }.toSeq

TaskKey[Unit]("cleanup") := {
  val dir = file(sys.props("user.home")) / ".ivy2" / "local" / organization.value
  IO.deleteFilesEmptyDirs(Seq(dir))
}

commands += Command.command("enableTemplatePlugin") { state =>
  val plugin = file(sys.props("scripted.template.plugin"))
  IO.copyFile(plugin, baseDirectory.value / "project" / plugin.getName)
  "reload" :: state
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
