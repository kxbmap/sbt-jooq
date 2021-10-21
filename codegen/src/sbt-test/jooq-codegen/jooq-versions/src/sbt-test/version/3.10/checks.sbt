import sbtjooq.codegen.internal._

TaskKey[Unit]("checkCodegenJavaOptions") := {
  val opts = (JooqCodegen / run / javaOptions).value
  val jv = JavaVersion.get((JooqCodegen / run / javaHome).value).major
  val xs = Seq("--add-modules", "java.xml.bind")
  if (jv >= 9 && jv < 11) {
    if (!opts.containsSlice(xs))
      sys.error(s"javaOptions should contains: --add-modules java.xml.bind (Java: $jv)")
  } else {
    if (opts.containsSlice(xs))
      sys.error(s"javaOptions should not contains: --add-modules java.xml.bind (Java: $jv)")
  }
}

TaskKey[Unit]("checkCodegenDependencies") := {
  val deps = libraryDependencies.value.filter(_.configurations.contains(JooqCodegen.name))
  val jv = JavaVersion.get((JooqCodegen / run / javaHome).value).major
  val xs = Seq(
    "javax.activation" % "activation" % "???",
    "javax.xml.bind" % "jaxb-api" % "???",
    "com.sun.xml.bind" % "jaxb-core" % "???",
    "com.sun.xml.bind" % "jaxb-impl" % "???",
  )
  if (jv >= 11) {
    if (!xs.forall(x => deps.exists(m => x.organization == m.organization && x.name == m.name)))
      sys.error(s"libraryDependencies should contains JAXB libraries (Java: $jv)")
  } else {
    if (xs.exists(x => deps.exists(m => x.organization == m.organization && x.name == m.name)))
      sys.error(s"libraryDependencies should not contains JAXB libraries (Java: $jv)")
  }
}

TaskKey[Unit]("checkJavacOptions") := {
  val opts = (Compile / javacOptions).value
  val jv = JavaVersion.get((Compile / compile / javaHome).value).major
  val xs = Seq("--add-modules", "java.xml.ws.annotation")
  if (jv >= 9 && jv < 11) {
    if (!opts.containsSlice(xs))
      sys.error(s"javaOptions should contains: --add-modules java.xml.ws.annotation (Java: $jv)")
  } else {
    if (opts.containsSlice(xs))
      sys.error(s"javaOptions should not contains: --add-modules java.xml.ws.annotation (Java: $jv)")
  }
}

TaskKey[Unit]("checkCompileDependencies") := {
  val deps = libraryDependencies.value.filter(_.configurations.contains(Compile.name))
  val jv = JavaVersion.get((Compile / compile / javaHome).value).major
  val x = "javax.annotation" % "javax.annotation-api" % "???"
  if (jv >= 11) {
    if (!deps.exists(m => x.organization == m.organization && x.name == m.name))
      sys.error(s"libraryDependencies should contains javax.annotation (Java: $jv)")
  } else {
    if (deps.exists(m => x.organization == m.organization && x.name == m.name))
      sys.error(s"libraryDependencies should not contains javax.annotation libraries (Java: $jv)")
  }
}
