package sbtjooq.codegen.internal

import java.util.Properties
import sbt._
import sbtjooq.codegen.BuildInfo
import sbtjooq.codegen.internal.JavaUtil._
import scala.collection.JavaConverters._
import scala.xml.{Elem, Node, NodeSeq, Text}

object Codegen {

  def javaVersion(javaHome: Option[File]): Int =
    javaHome.map(parseJavaVersion).getOrElse(majorVersion(sys.props("java.version")))

  def mainClass: String =
    "sbtjooq.codegen.tool.GenerationTool"

  def dependencies(jooqVersion: String, javaVersion: Int): Seq[ModuleID] =
    codegenToolDependencies ++ jaxbDependencies(jooqVersion, javaVersion)

  def javaOptions(jooqVersion: String, javaVersion: Int): Seq[String] =
    jaxbAddModulesOption(jooqVersion, javaVersion)

  def needsFork(jooqVersion: String, javaVersion: Int): Boolean =
    javaOptions(jooqVersion, javaVersion).nonEmpty

  def compileDependencies(javaVersion: Int, codegenJooqVersion: String, codegenJavaVersion: Int): Seq[ModuleID] =
    javaxAnnotationDependencies(javaVersion, codegenJooqVersion, codegenJavaVersion)

  def javacOptions(javaVersion: Int, codegenJooqVersion: String, codegenJavaVersion: Int): Seq[String] =
    javaxAnnotationAddModulesOption(javaVersion, codegenJooqVersion, codegenJavaVersion)


  private def codegenToolDependencies: Seq[ModuleID] =
    Seq("com.github.kxbmap" % "sbt-jooq-codegen-tool" % BuildInfo.sbtJooqVersion)

  //noinspection SbtDependencyVersionInspection
  private def jaxbDependencies(jooqVersion: String, javaVersion: Int): Seq[ModuleID] =
    if (needsJaxbSettings(jooqVersion) && !isJavaEEModulesBundled(javaVersion))
      Seq(
        "javax.activation" % "activation" % BuildInfo.javaxActivationVersion,
        "javax.xml.bind" % "jaxb-api" % BuildInfo.jaxbApiVersion,
        "com.sun.xml.bind" % "jaxb-core" % BuildInfo.jaxbCoreVersion,
        "com.sun.xml.bind" % "jaxb-impl" % BuildInfo.jaxbImplVersion)
    else
      Nil

  private def jaxbAddModulesOption(jooqVersion: String, javaVersion: Int): Seq[String] =
    if (needsJaxbSettings(jooqVersion) && isJigsawEnabled(javaVersion) && isJavaEEModulesBundled(javaVersion))
      Seq("--add-modules", "java.xml.bind")
    else
      Nil

  private def needsJaxbSettings(jooqVersion: String): Boolean =
    CrossVersion.partialVersion(jooqVersion).forall {
      case (x, y) => x < 3 || x == 3 && y <= 11
    }

  private def javaxAnnotationDependencies(
      javaVersion: Int,
      codegenJooqVersion: String,
      codegenJavaVersion: Int,
  ): Seq[ModuleID] =
    if (!isJavaEEModulesBundled(javaVersion)
      && !generatedAnnotationDisabledByDefault(codegenJooqVersion)
      && useJavaxAnnotationByDefault(codegenJooqVersion, codegenJavaVersion))
      Seq("javax.annotation" % "javax.annotation-api" % BuildInfo.javaxAnnotationApiVersion)
    else
      Nil

  private def javaxAnnotationAddModulesOption(
      javaVersion: Int,
      codegenJooqVersion: String,
      codegenJavaVersion: Int,
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

  private def useJavaxAnnotationByDefault(jooqVersion: String, javaVersion: Int): Boolean =
    javaVersion <= 8 || CrossVersion.partialVersion(jooqVersion).forall {
      case (x, y) => x < 3 || x == 3 && y < 12
    }


  def configTransformer(target: File, vars: Map[String, Any], expand: Any => NodeSeq): Node => Node =
    appendGeneratorTargetDirectory(target).andThen(replaceConfigVariables(vars, expand))

  def appendGeneratorTargetDirectory(target: File): Node => Node = {
    def append(node: Node, child: Node): Node =
      (node, child) match {
        case (e@Elem(p, l, a, s, xs@_*), x) if x.isAtom && e.text.trim.isEmpty =>
          Elem(p, l, a, s, false, xs :+ x: _*)
        case (Elem(p, l, a, s, xs@_*), c@Elem(_, _, _, _, cc@_*)) =>
          xs.span(_.label != c.label) match {
            case (ls, Seq(t, rs@_*)) =>
              Elem(p, l, a, s, false, ls ++ cc.foldLeft(t)(append) ++ rs: _*)
            case _ =>
              Elem(p, l, a, s, false, xs :+ c: _*)
          }
        case _ => node
      }
    val elem =
      <generator>
        <target>
          <directory>{target}</directory>
        </target>
      </generator>
    append(_, elem)
  }

  def replaceConfigVariables(vars: Map[String, Any], expand: Any => NodeSeq): Node => Node = {
    def go(parents: List[Node]): Node => NodeSeq = {
      def path = parents.reverseMap(_.label).mkString("/", "/", "")
      def replace(t: String): NodeSeq = {
        t.span(_ != '$') match {
          case (_, "") => Text(t)
          case (l, r) if r.startsWith("$$") => Text(l + "$") +: replace(r.drop(2))
          case (l, r) if r.startsWith("${") => Text(l) +: value(r.drop(2))
          case (_, r) => sys.error("Expected '$$' or '${' but " + r)
        }
      }
      def value(t: String): NodeSeq =
        t.span(_ != '}') match {
          case (_, "") => sys.error(s" Missing closing brace `}` at '$path'")
          case (k, r) =>
            val v = vars.getOrElse(k, sys.error(s"No variables found for key '$k' at '$path'"))
            expand(v) ++ replace(r.drop(1))
        }
      locally {
        case e@Elem(p, l, a, s, xs@_*) => Elem(p, l, a, s, xs.isEmpty, xs.flatMap(go(e :: parents)): _*)
        case Text(text) => replace(text)
        case node => node
      }
    }
    locally {
      case e@Elem(p, l, a, s, xs@_*) => Elem(p, l, a, s, xs.isEmpty, xs.flatMap(go(e :: Nil)): _*)
      case node => node
    }
  }

  def expandVariable: PartialFunction[Any, NodeSeq] = {
    case props: Properties =>
      props.entrySet().asScala.map { e =>
        <property>
          <key>{e.getKey}</key>
          <value>{e.getValue}</value>
        </property>
      }.toSeq
  }

  def expandVariableFallback: Any => NodeSeq =
    x => Text(x.toString)


  def generatorTargetDirectory(config: Node): File =
    (config \ "generator" \ "target" \ "directory").text.trim match {
      case "" => file("target") / "generated-sources" / "jooq"
      case text => file(text)
    }

  def generatorTargetPackage(config: Node): Seq[String] =
    (config \ "generator" \ "target" \ "packageName").text.trim match {
      case "" => Seq("org", "jooq", "generated")
      case text =>
        text.split('.').map(_.flatMap {
          case '_' | '-' | ' ' => "_"
          case c if c.isUnicodeIdentifierPart => c.toString
          case c if c <= 0xff => f"_${c.toInt}%02x"
          case c => f"_${c.toInt}%04x"
        }).map {
          case s if s.headOption.exists(c => c != '_' && !c.isUnicodeIdentifierStart) => s"_$s"
          case s => s
        }.toSeq
    }

}
