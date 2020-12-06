package sbtjooq.codegen.internal

import sbt._
import sbtjooq.codegen.BuildInfo
import sbtjooq.codegen.internal.JavaUtil._

object Codegen {

  def runtimeJavaVersion(javaHome: Option[File]): Option[String] =
    javaHome.map(parseJavaVersion).orElse(compileJavaVersion)

  def compileJavaVersion: Option[String] =
    sys.props.get("java.version")

  def mainClass(jooqVersion: String): String =
    CrossVersion.partialVersion(jooqVersion) match {
      case Some((x, y)) if x > 3 || x == 3 && y >= 11 => "org.jooq.codegen.GenerationTool"
      case _ => "org.jooq.util.GenerationTool"
    }

  private def hasModuleDeps(jooqVersion: String): Boolean =
    CrossVersion.partialVersion(jooqVersion).forall {
      case (x, y) => x < 3 || x == 3 && y < 12
    }

  def jaxbDependencies(jooqVersion: String, javaVersion: Option[String]): Seq[ModuleID] =
    if (hasModuleDeps(jooqVersion) && !javaVersion.forall(isJAXBBundled))
      Seq(
        "javax.activation" % "activation" % BuildInfo.javaxActivationVersion,
        "javax.xml.bind" % "jaxb-api" % BuildInfo.jaxbApiVersion,
        "com.sun.xml.bind" % "jaxb-core" % BuildInfo.jaxbCoreVersion,
        "com.sun.xml.bind" % "jaxb-impl" % BuildInfo.jaxbImplVersion)
    else
      Nil

  def jaxbAddModulesOption(jooqVersion: String, javaVersion: Option[String]): Seq[String] =
    if (hasModuleDeps(jooqVersion) && javaVersion.exists(v => isJigsawEnabled(v) && isJAXBBundled(v)))
      Seq("--add-modules", "java.xml.bind")
    else
      Nil

  def javaxAnnotationDependencies(jooqVersion: String, javaVersion: Option[String]): Seq[ModuleID] =
    if (hasModuleDeps(jooqVersion) && !javaVersion.forall(isJavaxAnnotationBundled))
      Seq("javax.annotation" % "javax.annotation-api" % BuildInfo.javaxAnnotationApiVersion)
    else
      Nil

  def javaxAnnotationAddModulesOption(jooqVersion: String, javaVersion: Option[String]): Seq[String] =
    if (hasModuleDeps(jooqVersion) && javaVersion.exists(v => isJigsawEnabled(v) && isJavaxAnnotationBundled(v)))
      Seq("--add-modules", "java.xml.ws.annotation")
    else
      Nil

}
