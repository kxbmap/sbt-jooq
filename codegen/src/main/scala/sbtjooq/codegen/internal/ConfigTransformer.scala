package sbtjooq.codegen.internal

import sbt._
import scala.xml.{Elem, Node, NodeSeq, Text}

object ConfigTransformer {

  def apply(target: File, vars: Map[String, Any], expand: Any => NodeSeq): Node => Node =
    appendGeneratorTargetDirectory(target).andThen(replaceConfigVariables(vars, expand))

  def appendGeneratorTargetDirectory(target: File): Node => Node = {
    def append(node: Node, child: Node): Node =
      (node, child) match {
        case (e @ Elem(p, l, a, s, xs @ _*), x) if x.isAtom && e.text.trim.isEmpty =>
          Elem(p, l, a, s, false, xs :+ x: _*)
        case (Elem(p, l, a, s, xs @ _*), c @ Elem(_, _, _, _, cc @ _*)) =>
          xs.span(_.label != c.label) match {
            case (ls, Seq(t, rs @ _*)) =>
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
      def replace(t: String): NodeSeq =
        t.span(_ != '$') match {
          case (_, "") => Text(t)
          case (l, r) if r.startsWith("$$") => Text(l + "$") +: replace(r.drop(2))
          case (l, r) if r.startsWith("${") => Text(l) +: value(r.drop(2))
          case (_, r) => sys.error("Expected '$$' or '${' but " + r)
        }
      def value(t: String): NodeSeq =
        t.span(_ != '}') match {
          case (_, "") => sys.error(s"Missing closing brace `}` at '$path'")
          case (k, r) =>
            val v = vars.getOrElse(k, sys.error(s"No variables found for key '$k' at '$path'"))
            expand(v) ++ replace(r.drop(1))
        }
      locally {
        case e @ Elem(p, l, a, s, xs @ _*) => Elem(p, l, a, s, xs.isEmpty, xs.flatMap(go(e :: parents)): _*)
        case Text(text) => replace(text)
        case node => node
      }
    }
    locally {
      case e @ Elem(p, l, a, s, xs @ _*) => Elem(p, l, a, s, xs.isEmpty, xs.flatMap(go(e :: Nil)): _*)
      case node => node
    }
  }

}
