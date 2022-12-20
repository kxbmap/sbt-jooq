enablePlugins(SbtPlugin)

publish / skip := true

scriptedBufferLog := false

scriptedLaunchOpts ++=
  sys.props.collect {
    case (k, v) if k.startsWith("scripted.") => s"-D$k=$v"
  }.toSeq

lazy val copy = project
  .settings(
    autoScalaLibrary := false,
    libraryDependencies += "org.jooq" % "jooq" % sys.props("scripted.jooq.version")
  )

TaskKey[Unit]("copyLibs") := {
  val jars = (copy / Compile / managedClasspath).value.files.map { f =>
    f -> sbtTestDirectory.value / "jooq" / "auto-library" / "lib" / f.getName
  }
  IO.copy(jars)
}
