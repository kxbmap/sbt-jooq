/*
 * Copyright 2015 Tsukasa Kitachi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package sbtjooq.codegen.internal

import sbt.*
import scala.xml.{Elem, Node, NodeSeq, Text}

object ConfigTransformer {

  def apply(target: File, expander: VariableExpander): ConfigTransformer =
    appendGeneratorTargetDirectory(target).andThen(replaceConfigVariables(expander))

  def appendGeneratorTargetDirectory(target: File): ConfigTransformer = {
    def append(node: Node, child: Node): Node =
      (node, child) match {
        case (e @ Elem(p, l, a, s, xs*), x) if x.isAtom && e.text.trim.isEmpty =>
          Elem(p, l, a, s, false, (xs :+ x)*)
        case (Elem(p, l, a, s, xs*), c @ Elem(_, _, _, _, cc*)) =>
          xs.span(_.label != c.label) match {
            case (ls, Seq(t, rs*)) =>
              Elem(p, l, a, s, false, (ls ++ cc.foldLeft(t)(append) ++ rs)*)
            case _ =>
              Elem(p, l, a, s, false, (xs :+ c)*)
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

  def replaceConfigVariables(expander: VariableExpander): ConfigTransformer = {
    def go(parents: List[Node]): Node => NodeSeq = {
      def path = parents.reverseMap(_.label).mkString("/", "/", "")
      def expand(k: String): NodeSeq = expander(k).getOrElse(sys.error(s"$path: No variables found for key '$k'"))
      def replace(t: String): NodeSeq =
        t.span(_ != '$') match {
          case (_, "") => Text(t)
          case (l, r) if r.startsWith("$$") => Text(l + "$") +: replace(r.drop(2))
          case (l, r) if r.startsWith("${") => Text(l) +: value(r.drop(2))
          case (_, r) => sys.error("Expected '$$' or '${' but " + r)
        }
      def value(t: String): NodeSeq =
        t.span(_ != '}') match {
          case (_, "") => sys.error(s"$path: Missing closing brace `}`")
          case (k, r) => expand(k) ++ replace(r.drop(1))
        }
      locally {
        case e @ Elem(p, l, a, s, xs*) => Elem(p, l, a, s, xs.isEmpty, xs.flatMap(go(e :: parents))*)
        case Text(text) => replace(text)
        case node => node
      }
    }
    locally {
      case e @ Elem(p, l, a, s, xs*) => Elem(p, l, a, s, xs.isEmpty, xs.flatMap(go(e :: Nil))*)
      case node => node
    }
  }

}
