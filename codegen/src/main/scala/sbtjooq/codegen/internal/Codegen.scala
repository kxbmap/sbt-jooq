package sbtjooq.codegen.internal

import sbt._
import sbtjooq.codegen.BuildInfo
import sbtjooq.codegen.internal.JavaUtil._

object Codegen {

  def javaVersion(javaHome: Option[File]): String =
    javaHome.map(parseJavaVersion).getOrElse(compileJavaVersion)

  def compileJavaVersion: String =
    sys.props("java.version")

  def mainClass(jooqVersion: String): String =
    CrossVersion.partialVersion(jooqVersion) match {
      case Some((x, y)) if x > 3 || x == 3 && y >= 11 => "org.jooq.codegen.GenerationTool"
      case _ => "org.jooq.util.GenerationTool"
    }

  def dependencies(jooqVersion: String, javaVersion: String): Seq[ModuleID] =
    codegenToolDependencies ++ jaxbDependencies(jooqVersion, javaVersion)

  def javaOptions(jooqVersion: String, javaVersion: String): Seq[String] =
    jaxbAddModulesOption(jooqVersion, javaVersion)

  def needsFork(jooqVersion: String, javaVersion: String): Boolean =
    javaOptions(jooqVersion, javaVersion).nonEmpty

  def compileDependencies(jooqVersion: String, javaVersion: String): Seq[ModuleID] =
    javaxAnnotationDependencies(jooqVersion, javaVersion)

  def javacOptions(jooqVersion: String, javaVersion: String): Seq[String] =
    javaxAnnotationAddModulesOption(jooqVersion, javaVersion)


  private def codegenToolDependencies: Seq[ModuleID] =
    Seq("com.github.kxbmap" % "sbt-jooq-codegen-tool" % BuildInfo.sbtJooqVersion)

  private def hasModuleDeps(jooqVersion: String): Boolean =
    CrossVersion.partialVersion(jooqVersion).forall {
      case (x, y) => x < 3 || x == 3 && y <= 11
    }

  private def jaxbDependencies(jooqVersion: String, javaVersion: String): Seq[ModuleID] =
    if (hasModuleDeps(jooqVersion) && !isJAXBBundled(javaVersion))
      Seq(
        "javax.activation" % "activation" % BuildInfo.javaxActivationVersion,
        "javax.xml.bind" % "jaxb-api" % BuildInfo.jaxbApiVersion,
        "com.sun.xml.bind" % "jaxb-core" % BuildInfo.jaxbCoreVersion,
        "com.sun.xml.bind" % "jaxb-impl" % BuildInfo.jaxbImplVersion)
    else
      Nil

  private def jaxbAddModulesOption(jooqVersion: String, javaVersion: String): Seq[String] =
    if (hasModuleDeps(jooqVersion) && isJigsawEnabled(javaVersion) && isJAXBBundled(javaVersion))
      Seq("--add-modules", "java.xml.bind")
    else
      Nil

  private def javaxAnnotationDependencies(jooqVersion: String, javaVersion: String): Seq[ModuleID] =
    if (hasModuleDeps(jooqVersion) && !isJavaxAnnotationBundled(javaVersion))
      Seq("javax.annotation" % "javax.annotation-api" % BuildInfo.javaxAnnotationApiVersion)
    else
      Nil

  private def javaxAnnotationAddModulesOption(jooqVersion: String, javaVersion: String): Seq[String] =
    if (hasModuleDeps(jooqVersion) && isJigsawEnabled(javaVersion) && isJavaxAnnotationBundled(javaVersion))
      Seq("--add-modules", "java.xml.ws.annotation")
    else
      Nil

}
