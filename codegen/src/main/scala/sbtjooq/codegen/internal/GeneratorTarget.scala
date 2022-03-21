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
import scala.xml.Node

object GeneratorTarget {

  def get(config: Node): File = {
    val target = directory(config).getAbsoluteFile
    packageName(config).foldLeft(target)(_ / _)
  }

  def directory(config: Node): File =
    (config \ "generator" \ "target" \ "directory").text.trim match {
      case "" => file("target") / "generated-sources" / "jooq"
      case text => file(text)
    }

  def packageName(config: Node): Seq[String] =
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
