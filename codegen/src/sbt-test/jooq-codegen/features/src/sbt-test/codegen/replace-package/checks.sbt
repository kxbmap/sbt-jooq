val expected = "com.example._42_f_2fo_b_40r_b_z._ff20漢字_3000_d867_de3d"

TaskKey[Unit]("check") := {
  val generated = (Compile / jooqCodegenGeneratedSources).value
  if (generated.isEmpty)
    sys.error("Empty jooqCodegenGeneratedSources")

  generated.foreach { f =>
    val pkg = IO.readLines(f).find(_.matches("package .+;"))
    if (!pkg.exists(_.contains(expected)))
      sys.error(s"Not expected: ${pkg.getOrElse("No package line detected")} ($f)")
  }
}
