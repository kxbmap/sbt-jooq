import sbtjooq.codegen.internal.Codegen
import sbtjooq.codegen.internal.JavaUtil

TaskKey[Unit]("checkCodegenJavaOptions") := {
  val opts = (JooqCodegen / run / javaOptions).value
  val jv = Codegen.javaVersion((JooqCodegen / javaHome).value)
  val xs = Seq("--add-modules", "java.xml.bind")
  if (JavaUtil.isJigsawEnabled(jv) && JavaUtil.isJAXBBundled(jv)) {
    if (!opts.containsSlice(xs))
      sys.error(s"javaOptions should contains: --add-modules java.xml.bind (Java: $jv)")
  } else {
    if (opts.containsSlice(xs))
      sys.error(s"javaOptions should not contains: --add-modules java.xml.bind (Java: $jv)")
  }
}

TaskKey[Unit]("checkCodegenDependencies") := {
  val deps = libraryDependencies.value.filter(_.configurations.contains(JooqCodegen.name))
  val jv = Codegen.javaVersion((JooqCodegen / javaHome).value)
  val xs = Seq(
    "javax.activation" % "activation" % "???",
    "javax.xml.bind" % "jaxb-api" % "???",
    "com.sun.xml.bind" % "jaxb-core" % "???",
    "com.sun.xml.bind" % "jaxb-impl" % "???"
  )
  if (!JavaUtil.isJAXBBundled(jv)) {
    if (!xs.forall(x => deps.exists(m => x.organization == m.organization && x.name == m.name)))
      sys.error(s"libraryDependencies should contains JAXB libraries (Java: $jv)")
  } else {
    if (xs.exists(x => deps.exists(m => x.organization == m.organization && x.name == m.name)))
      sys.error(s"libraryDependencies should not contains JAXB libraries (Java: $jv)")
  }
}

TaskKey[Unit]("checkJavacOptions") := {
  val opts = (Compile / javacOptions).value
  val jv = sys.props("java.version")
  val xs = Seq("--add-modules", "java.xml.ws.annotation")
  if (JavaUtil.isJigsawEnabled(jv) && JavaUtil.isJavaxAnnotationBundled(jv)) {
    if (!opts.containsSlice(xs))
      sys.error(s"javaOptions should contains: --add-modules java.xml.ws.annotation (Java: $jv)")
  } else {
    if (opts.containsSlice(xs))
      sys.error(s"javaOptions should not contains: --add-modules java.xml.ws.annotation (Java: $jv)")
  }
}

TaskKey[Unit]("checkCompileDependencies") := {
  val deps = libraryDependencies.value.filter(_.configurations.contains(Compile.name))
  val jv = sys.props("java.version")
  val x = "javax.annotation" % "javax.annotation-api" % "???"
  if (!JavaUtil.isJavaxAnnotationBundled(jv)) {
    if (!deps.exists(m => x.organization == m.organization && x.name == m.name))
      sys.error(s"libraryDependencies should contains javax.annotation (Java: $jv)")
  } else {
    if (deps.exists(m => x.organization == m.organization && x.name == m.name))
      sys.error(s"libraryDependencies should not contains javax.annotation libraries (Java: $jv)")
  }
}
