Seq(JooqCodegen, Compile).flatMap { c =>
  Seq(
    c / run / fork := true,
    c / run / javaHome := {
      val key = "RUNTIME_JAVA_HOME"
      (if (insideCI.value) Some(sys.env(key)) else sys.env.get(key)).map(file)
    },
  )
}
