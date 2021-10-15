package sbtjooq.codegen.internal

import java.util.Properties
import sbt._
import sbtjooq.codegen.BuildInfo
import sbtjooq.JooqVersion
import scala.collection.JavaConverters._
import scala.xml.{Elem, Node, NodeSeq, Text}

object Codegen {

  def javaVersion(javaHome: Option[File]): JavaVersion =
    javaHome.map(parseJavaVersion).getOrElse(JavaVersion(sys.props("java.version")))

  def mainClass: String =
    "sbtjooq.codegen.tool.GenerationTool"

  def dependencies(auto: Boolean, jooqVersion: JooqVersion, javaVersion: JavaVersion): Seq[ModuleID] =
    codegenToolDependencies ++ (if (auto) jaxbDependencies(jooqVersion, javaVersion) else Nil)

  def javaOptions(auto: Boolean, jooqVersion: JooqVersion, javaVersion: JavaVersion): Seq[String] =
    if (auto) jaxbAddModulesOption(jooqVersion, javaVersion) else Nil

  def needsFork(auto: Boolean, jooqVersion: JooqVersion, javaVersion: JavaVersion): Boolean =
    javaOptions(auto, jooqVersion, javaVersion).nonEmpty

  def compileDependencies(auto: Boolean, javaVersion: JavaVersion, codegenVersions: CodegenVersions): Seq[ModuleID] =
    if (auto) javaxAnnotationDependencies(javaVersion, codegenVersions) else Nil

  def javacOptions(auto: Boolean, javaVersion: JavaVersion, codegenVersions: CodegenVersions): Seq[String] =
    if (auto) javaxAnnotationAddModulesOption(javaVersion, codegenVersions) else Nil


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

  private def javaxAnnotationDependencies(javaVersion: JavaVersion, codegenVersions: CodegenVersions): Seq[ModuleID] =
    if (!javaVersion.isJavaEEModulesBundled
      && !codegenVersions.jooq.generatedAnnotationDisabledByDefault
      && codegenVersions.useJavaxAnnotationByDefault)
      Seq("javax.annotation" % "javax.annotation-api" % BuildInfo.javaxAnnotationApiVersion)
    else
      Nil

  private def javaxAnnotationAddModulesOption(javaVersion: JavaVersion, codegenVersions: CodegenVersions): Seq[String] =
    if (javaVersion.isJigsawEnabled
      && javaVersion.isJavaEEModulesBundled
      && !codegenVersions.jooq.generatedAnnotationDisabledByDefault
      && codegenVersions.useJavaxAnnotationByDefault)
      Seq("--add-modules", "java.xml.ws.annotation")
    else
      Nil


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
          case (_, "") => sys.error(s"Missing closing brace `}` at '$path'")
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
