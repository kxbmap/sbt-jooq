enablePlugins(SbtPlugin)

scriptedBatchExecution := true
scriptedParallelInstances := 2

scriptedLaunchOpts ++=
  sys.props.collect {
    case (k, v) if k.startsWith("scripted.") => s"-D$k=$v"
  }.toSeq

TaskKey[Unit]("cleanup") := {
  val dir = file(sys.props("user.home")) / ".ivy2" / "local" / organization.value
  IO.deleteFilesEmptyDirs(Seq(dir))
}
