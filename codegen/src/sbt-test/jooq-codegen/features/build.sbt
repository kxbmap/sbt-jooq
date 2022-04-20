enablePlugins(SbtPlugin)

scriptedBatchExecution := true
scriptedParallelInstances := 2

scriptedLaunchOpts ++=
  sys.props.collect {
    case (k, v) if k.startsWith("scripted.") => s"-D$k=$v"
  }.toSeq

scriptedLaunchOpts ++= {
  val urls = (0 to 1).map { n =>
    val setup = (baseDirectory.value / "sql" / s"setup$n.sql").toString.replace('\\', '/')
    s"jdbc:h2:mem:;INIT=runscript from '$setup'"
  }
  s"-Dscripted.jdbc.url=${urls.head}" +:
    urls.zipWithIndex.map {
      case (url, n) => s"-Dscripted.jdbc.url.$n=$url"
    }
}

TaskKey[Unit]("cleanup") := {
  val dir = file(sys.props("user.home")) / ".ivy2" / "local" / organization.value
  IO.deleteFilesEmptyDirs(Seq(dir))
}
