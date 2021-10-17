package sbtjooq.codegen.internal

import sbt._
import sbtjooq.codegen.BuildInfo
import sbtjooq.JooqVersion

object Codegen {

  def mainClass: String = "sbtjooq.codegen.tool.GenerationTool"

  def dependencies(auto: Boolean, jooqVersion: JooqVersion, javaHome: Option[File]): Seq[ModuleID] =
    codegenToolDependencies ++
      (if (auto) jaxbDependencies(jooqVersion, JavaVersion.get(javaHome)) else Nil)

  def javaOptions(auto: Boolean, jooqVersion: JooqVersion, javaHome: Option[File]): Seq[String] =
    if (auto) jaxbAddModulesOption(jooqVersion, JavaVersion.get(javaHome)) else Nil

  def needsFork(auto: Boolean, jooqVersion: JooqVersion, javaHome: Option[File]): Boolean =
    javaOptions(auto, jooqVersion, javaHome).nonEmpty

  def compileDependencies(
      auto: Boolean,
      javaHome: Option[File],
      codegenJooqVersion: JooqVersion,
      codegenJavaHome: Option[File],
  ): Seq[ModuleID] =
    if (auto)
      javaxAnnotationDependencies(JavaVersion.get(javaHome), codegenJooqVersion, JavaVersion.get(codegenJavaHome))
    else
      Nil

  def javacOptions(
      auto: Boolean,
      javaHome: Option[File],
      codegenJooqVersion: JooqVersion,
      codegenJavaHome: Option[File],
  ): Seq[String] =
    if (auto)
      javaxAnnotationAddModulesOption(JavaVersion.get(javaHome), codegenJooqVersion, JavaVersion.get(codegenJavaHome))
    else
      Nil


  private def codegenToolDependencies: Seq[ModuleID] =
    Seq("com.github.kxbmap" % "sbt-jooq-codegen-tool" % BuildInfo.sbtJooqVersion)

  //noinspection SbtDependencyVersionInspection
  private def jaxbDependencies(jooqVersion: JooqVersion, javaVersion: JavaVersion): Seq[ModuleID] =
    if (jooqVersion.needsJaxbSettings && !javaVersion.isJavaEEModulesBundled)
      Seq(
        "javax.activation" % "activation" % BuildInfo.javaxActivationVersion,
        "javax.xml.bind" % "jaxb-api" % BuildInfo.jaxbApiVersion,
        "com.sun.xml.bind" % "jaxb-core" % BuildInfo.jaxbCoreVersion,
        "com.sun.xml.bind" % "jaxb-impl" % BuildInfo.jaxbImplVersion)
    else
      Nil

  private def jaxbAddModulesOption(jooqVersion: JooqVersion, javaVersion: JavaVersion): Seq[String] =
    if (jooqVersion.needsJaxbSettings && javaVersion.isJigsawEnabled && javaVersion.isJavaEEModulesBundled)
      Seq("--add-modules", "java.xml.bind")
    else
      Nil

  private def javaxAnnotationDependencies(
      javaVersion: JavaVersion,
      codegenJooqVersion: JooqVersion,
      codegenJavaVersion: JavaVersion,
  ): Seq[ModuleID] =
    if (!javaVersion.isJavaEEModulesBundled
      && !codegenJooqVersion.generatedAnnotationDisabledByDefault
      && (codegenJooqVersion, codegenJavaVersion).useJavaxAnnotationByDefault)
      Seq("javax.annotation" % "javax.annotation-api" % BuildInfo.javaxAnnotationApiVersion)
    else
      Nil

  private def javaxAnnotationAddModulesOption(
      javaVersion: JavaVersion,
      codegenJooqVersion: JooqVersion,
      codegenJavaVersion: JavaVersion,
  ): Seq[String] =
    if (javaVersion.isJigsawEnabled
      && javaVersion.isJavaEEModulesBundled
      && !codegenJooqVersion.generatedAnnotationDisabledByDefault
      && (codegenJooqVersion, codegenJavaVersion).useJavaxAnnotationByDefault)
      Seq("--add-modules", "java.xml.ws.annotation")
    else
      Nil

  private implicit class JooqVersionOps(jooqVersion: JooqVersion) {
    def needsJaxbSettings: Boolean =
      jooqVersion.matches("<=3.11")

    def generatedAnnotationDisabledByDefault: Boolean =
      jooqVersion.matches(">=3.13")
  }

  private implicit class CodegenVersionsOps(versions: (JooqVersion, JavaVersion)) {
    def jooq: JooqVersion = versions._1
    def java: JavaVersion = versions._2

    def useJavaxAnnotationByDefault: Boolean =
      java.major <= 8 || jooq.matches("<3.12")
  }

}
