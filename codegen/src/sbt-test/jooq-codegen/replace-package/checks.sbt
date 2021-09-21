val core = LocalProject("core")

val expected = "com.example._42_foo_b_40r_b_2az.漢字__d867_de3d"

TaskKey[Unit]("check") := {
  val generated = (core / Compile / jooqCodegenGeneratedSources).value
  if (generated.isEmpty)
    sys.error("Empty jooqCodegenGeneratedSources")

  generated.foreach { f =>
    val pkg = IO.readLines(f).find(_.matches("package .+;"))
    if (!pkg.exists(_.contains(expected)))
      sys.error(s"Not expected: ${pkg.getOrElse("No package line detected")}")
  }
}
