enablePlugins(SbtPlugin)

scriptedBufferLog := false

scriptedLaunchOpts ++=
  sys.props.collect {
    case (k, v) if k.startsWith("scripted.") => s"-D$k=$v"
  }.toSeq

lazy val dep = project
  .settings(
    autoScalaLibrary := false,
    libraryDependencies += "org.jooq" % "jooq" % sys.props("scripted.jooq.version"),
  )

lazy val copyLibs = taskKey[Unit]("")
lazy val cleanup = taskKey[Unit]("")

copyLibs := {
  val jars = (dep / Compile / managedClasspath).value.files.map {
    f => f -> sbtTestDirectory.value / "jooq" / "auto-library" / "lib" / f.getName
  }
  IO.copy(jars)
}

cleanup := {
  val dir = file(sys.props("user.home")) / ".ivy2" / "local" / organization.value / name.value
  IO.deleteFilesEmptyDirs(Seq(dir))
}
