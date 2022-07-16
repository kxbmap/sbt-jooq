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

package sbtjooq.codegen

import sbt.*
import scala.language.implicitConversions
import scala.xml.{Elem, Node, NodeBuffer}

sealed trait JooqCodegenConfig

object JooqCodegenConfig {

  sealed trait Single extends JooqCodegenConfig

  case class FromFile(file: File) extends Single

  case class FromResource(resource: String) extends Single

  case class FromXML(xml: Node) extends Single

  case class Sequence(seq: Seq[Single]) extends JooqCodegenConfig

  implicit class CodegenConfigOps(config: JooqCodegenConfig) {
    def toSeq: Seq[Single] =
      config match {
        case single: Single => Seq(single)
        case Sequence(seq) => seq
      }

    def isEmpty: Boolean =
      config match {
        case Sequence(seq) => seq.isEmpty
        case _ => false
      }

    def +(other: Single): JooqCodegenConfig =
      Sequence(toSeq :+ other)

    def ++(other: JooqCodegenConfig): JooqCodegenConfig =
      Sequence(toSeq ++ other.toSeq)
  }

  def empty: JooqCodegenConfig = Sequence(Seq.empty)

  def fromURI(uri: URI): Single =
    uri.getScheme match {
      case "classpath" => FromResource(uri.getSchemeSpecificPart)
      case "file" => FromFile(new File(uri))
      case _ => throw new IllegalArgumentException(s"Unknown scheme: $uri")
    }

  def fromURIString(uri: String): Single = fromURI(sbt.uri(uri))

  implicit def fileToCodegenConfig(file: File): Single = FromFile(file)

  implicit def xmlElemToCodegenConfig(xml: Elem): Single = FromXML(xml)

  implicit def uriToCodegenConfig(uri: URI): Single = fromURI(uri)

  implicit def seqToCodegenConfig[A](seq: Seq[A])(implicit ev: A => JooqCodegenConfig): Sequence =
    Sequence(seq.flatMap(ev(_).toSeq))

  implicit def nodeBufferToCodegenConfig(buffer: NodeBuffer): Sequence =
    Sequence(buffer.map(FromXML))

  implicit val appendCodegenConfigToCodegenConfig: Append.Values[JooqCodegenConfig, JooqCodegenConfig] = _ ++ _

  implicit def appendSingleToCodegenConfig[A](implicit ev: A => Single): Append.Value[JooqCodegenConfig, A] = _ + _

  implicit def appendSequenceToCodegenConfig[A](implicit ev: A => Sequence): Append.Values[JooqCodegenConfig, A] =
    _ ++ _

}
