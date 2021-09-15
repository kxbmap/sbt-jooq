package sbtjooq.codegen.internal

import sbt._
import sbtjooq.codegen.BuildInfo
import sbtjooq.codegen.internal.JavaUtil._

object Codegen {

  def runtimeJavaVersion(javaHome: Option[File]): String =
    javaHome.map(parseJavaVersion).getOrElse(compileJavaVersion)

  def compileJavaVersion: String =
    sys.props("java.version")

  def mainClass(jooqVersion: String): String =
    CrossVersion.partialVersion(jooqVersion) match {
      case Some((x, y)) if x > 3 || x == 3 && y >= 11 => "org.jooq.codegen.GenerationTool"
      case _ => "org.jooq.util.GenerationTool"
    }

  private def hasModuleDeps(jooqVersion: String): Boolean =
    CrossVersion.partialVersion(jooqVersion).forall {
      case (x, y) => x < 3 || x == 3 && y <= 11
    }

  def jaxbDependencies(jooqVersion: String, javaVersion: String): Seq[ModuleID] =
    if (hasModuleDeps(jooqVersion) && !isJAXBBundled(javaVersion))
      Seq(
        "javax.activation" % "activation" % BuildInfo.javaxActivationVersion,
        "javax.xml.bind" % "jaxb-api" % BuildInfo.jaxbApiVersion,
        "com.sun.xml.bind" % "jaxb-core" % BuildInfo.jaxbCoreVersion,
        "com.sun.xml.bind" % "jaxb-impl" % BuildInfo.jaxbImplVersion)
    else
      Nil

  def jaxbAddModulesOption(jooqVersion: String, javaVersion: String): Seq[String] =
    if (hasModuleDeps(jooqVersion) && isJigsawEnabled(javaVersion) && isJAXBBundled(javaVersion))
      Seq("--add-modules", "java.xml.bind")
    else
      Nil

  def javaxAnnotationDependencies(jooqVersion: String, javaVersion: String): Seq[ModuleID] =
    if (hasModuleDeps(jooqVersion) && !isJavaxAnnotationBundled(javaVersion))
      Seq("javax.annotation" % "javax.annotation-api" % BuildInfo.javaxAnnotationApiVersion)
    else
      Nil

  def javaxAnnotationAddModulesOption(jooqVersion: String, javaVersion: String): Seq[String] =
    if (hasModuleDeps(jooqVersion) && isJigsawEnabled(javaVersion) && isJavaxAnnotationBundled(javaVersion))
      Seq("--add-modules", "java.xml.ws.annotation")
    else
      Nil

}
