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
  val key = "RUNTIME_JAVA_HOME"
  val home = if (insideCI.value) Some(sys.env(key)) else sys.env.get(key)
  val version = home.map(file(_).getName.dropWhile(!_.isDigit).stripPrefix("1.").takeWhile(_.isDigit).toInt)
  if (version.exists(_ < 11))
    IO.touch(sbtTestDirectory.value / "version" / "3.15" / "disabled")
}
