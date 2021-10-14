import sbtjooq.codegen.internal.Codegen

TaskKey[Unit]("checkCodegenJavaOptions") := {
  val opts = (JooqCodegen / run / javaOptions).value
  val xs = Seq("--add-modules", "java.xml.bind")
  if (opts.containsSlice(xs))
    sys.error(s"javaOptions should not contains: --add-modules java.xml.bind")
}

TaskKey[Unit]("checkCodegenDependencies") := {
  val deps = libraryDependencies.value.filter(_.configurations.contains(JooqCodegen.name))
  val xs = Seq(
    "javax.activation" % "activation" % "???",
    "javax.xml.bind" % "jaxb-api" % "???",
    "com.sun.xml.bind" % "jaxb-core" % "???",
    "com.sun.xml.bind" % "jaxb-impl" % "???"
  )
  if (xs.exists(x => deps.exists(m => x.organization == m.organization && x.name == m.name)))
    sys.error(s"libraryDependencies should not contains JAXB libraries")
}

TaskKey[Unit]("checkJavacOptions") := {
  val opts = (Compile / javacOptions).value
  val xs = Seq("--add-modules", "java.xml.ws.annotation")
  if (opts.containsSlice(xs))
    sys.error(s"javaOptions should not contains: --add-modules java.xml.ws.annotation")
}

TaskKey[Unit]("checkCompileDependencies") := {
  val deps = libraryDependencies.value.filter(_.configurations.contains(Compile.name))
  val jv = Codegen.javaVersion((Compile / compile / javaHome).value).major
  val cjv = Codegen.javaVersion((JooqCodegen / run / javaHome).value).major
  val x = "javax.annotation" % "javax.annotation-api" % "???"
  if (jv >= 11 && cjv <= 8) {
    if (!deps.exists(m => x.organization == m.organization && x.name == m.name))
      sys.error(s"libraryDependencies should contains javax.annotation (Java compile: $jv, codegen: $cjv)")
  } else {
    if (deps.exists(m => x.organization == m.organization && x.name == m.name))
      sys.error(s"libraryDependencies should not contains javax.annotation libraries (Java compile: $jv, codegen: $cjv)")
  }
}
