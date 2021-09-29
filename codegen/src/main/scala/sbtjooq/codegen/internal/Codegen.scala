package sbtjooq.codegen.internal

import sbt._
import sbtjooq.codegen.BuildInfo
import sbtjooq.codegen.internal.JavaUtil._
import scala.xml.{Node, Text}
import scala.xml.transform.{RewriteRule, RuleTransformer}

object Codegen {

  def javaVersion(javaHome: Option[File]): String =
    javaHome.map(parseJavaVersion).getOrElse(sys.props("java.version"))

  def mainClass: String =
    "sbtjooq.codegen.tool.GenerationTool"

  def dependencies(jooqVersion: String, javaVersion: String): Seq[ModuleID] =
    codegenToolDependencies ++ jaxbDependencies(jooqVersion, javaVersion)

  def javaOptions(jooqVersion: String, javaVersion: String): Seq[String] =
    jaxbAddModulesOption(jooqVersion, javaVersion)

  def needsFork(jooqVersion: String, javaVersion: String): Boolean =
    javaOptions(jooqVersion, javaVersion).nonEmpty

  def compileDependencies(javaVersion: String, codegenJooqVersion: String, codegenJavaVersion: String): Seq[ModuleID] =
    javaxAnnotationDependencies(javaVersion, codegenJooqVersion, codegenJavaVersion)

  def javacOptions(javaVersion: String, codegenJooqVersion: String, codegenJavaVersion: String): Seq[String] =
    javaxAnnotationAddModulesOption(javaVersion, codegenJooqVersion, codegenJavaVersion)


  private def codegenToolDependencies: Seq[ModuleID] =
    Seq("com.github.kxbmap" % "sbt-jooq-codegen-tool" % BuildInfo.sbtJooqVersion)

  //noinspection SbtDependencyVersionInspection
  private def jaxbDependencies(jooqVersion: String, javaVersion: String): Seq[ModuleID] =
    if (needsJaxbSettings(jooqVersion) && !isJavaEEModulesBundled(javaVersion))
      Seq(
        "javax.activation" % "activation" % BuildInfo.javaxActivationVersion,
        "javax.xml.bind" % "jaxb-api" % BuildInfo.jaxbApiVersion,
        "com.sun.xml.bind" % "jaxb-core" % BuildInfo.jaxbCoreVersion,
        "com.sun.xml.bind" % "jaxb-impl" % BuildInfo.jaxbImplVersion)
    else
      Nil

  private def jaxbAddModulesOption(jooqVersion: String, javaVersion: String): Seq[String] =
    if (needsJaxbSettings(jooqVersion) && isJigsawEnabled(javaVersion) && isJavaEEModulesBundled(javaVersion))
      Seq("--add-modules", "java.xml.bind")
    else
      Nil

  private def needsJaxbSettings(jooqVersion: String): Boolean =
    CrossVersion.partialVersion(jooqVersion).forall {
      case (x, y) => x < 3 || x == 3 && y <= 11
    }

  private def javaxAnnotationDependencies(
      javaVersion: String,
      codegenJooqVersion: String,
      codegenJavaVersion: String,
  ): Seq[ModuleID] =
    if (!isJavaEEModulesBundled(javaVersion)
      && !generatedAnnotationDisabledByDefault(codegenJooqVersion)
      && useJavaxAnnotationByDefault(codegenJooqVersion, codegenJavaVersion))
      Seq("javax.annotation" % "javax.annotation-api" % BuildInfo.javaxAnnotationApiVersion)
    else
      Nil

  private def javaxAnnotationAddModulesOption(
      javaVersion: String,
      codegenJooqVersion: String,
      codegenJavaVersion: String,
  ): Seq[String] =
    if (isJigsawEnabled(javaVersion)
      && isJavaEEModulesBundled(javaVersion)
      && !generatedAnnotationDisabledByDefault(codegenJooqVersion)
      && useJavaxAnnotationByDefault(codegenJooqVersion, codegenJavaVersion))
      Seq("--add-modules", "java.xml.ws.annotation")
    else
      Nil

  private def generatedAnnotationDisabledByDefault(jooqVersion: String): Boolean =
    CrossVersion.partialVersion(jooqVersion).forall {
      case (x, y) => x > 3 || x == 3 && y >= 13
    }

  private def useJavaxAnnotationByDefault(jooqVersion: String, javaVersion: String): Boolean =
    major(javaVersion) <= 8 || CrossVersion.partialVersion(jooqVersion).forall {
      case (x, y) => x < 3 || x == 3 && y < 12
    }


  def configTransformer(vars: Map[String, String]): RuleTransformer =
    new RuleTransformer(new RewriteRule {
      val parser = new SubstitutionParser(vars)
      override def transform(n: Node): Seq[Node] = n match {
        case Text(data) =>
          parser.parse(data).fold(
            e => throw new MessageOnlyException(s"Substitution failure: $e"),
            s => Text(s)
          )
        case otherwise => otherwise
      }
    })

  def generatorTargetDirectory(config: Node): String =
    (config \ "generator" \ "target" \ "directory").text.trim match {
      case "" => "target/generated-sources/jooq"
      case text => text
    }

  def generatorTargetPackage(config: Node): Seq[String] =
    (config \ "generator" \ "target" \ "packageName").text.trim match {
      case "" => Seq("org", "jooq", "generated")
      case text =>
        text.split('.').map {
          case s if s.headOption.exists(!_.isUnicodeIdentifierStart) => s"_$s"
          case s => s
        }.map(_.flatMap {
          case '-' | ' ' => "_"
          case c if c.isUnicodeIdentifierPart => c.toString
          case c if c <= 0xff => f"_${c.toInt}%02x"
          case c => f"_${c.toInt}%04x"
        }).toSeq
    }

}
