val core = LocalProject("core")

val path = file("target/generated-sources/jooq")
val expected = "org.jooq.generated"

TaskKey[Unit]("check") := {
  val generated = (core / Compile / jooqCodegenGeneratedSources).value
  if (generated.isEmpty)
    sys.error("Empty jooqCodegenGeneratedSources")

  generated.foreach { f =>
    if (IO.relativize(path, f).isEmpty)
      sys.error(s"Not relative: $f")
  }
  generated.foreach { f =>
    val pkg = IO.readLines(f).find(_.matches("package .+;"))
    if (!pkg.exists(_.contains(expected)))
      sys.error(s"Not expected: ${pkg.getOrElse("No package line detected")}")
  }
}
