scriptedBufferLog := false
scriptedLaunchOpts ++= Seq(
  "-Xmx1024M",
  s"-Dplugin.version=${version.value}"
)
