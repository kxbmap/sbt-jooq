package sbtjooq.codegen

import sbt._
import scala.language.implicitConversions
import scala.xml.{Elem, Node, NodeBuffer}

sealed trait CodegenConfig {
  def toSeq: Seq[CodegenConfig.Single]
  def isEmpty: Boolean

  def +(other: CodegenConfig.Single): CodegenConfig =
    CodegenConfig.Sequence(toSeq :+ other)

  def ++(other: CodegenConfig): CodegenConfig =
    CodegenConfig.Sequence(toSeq ++ other.toSeq)
}

object CodegenConfig {

  sealed trait Single extends CodegenConfig {
    override def toSeq: Seq[Single] = Seq(this)
    override def isEmpty: Boolean = false
  }

  case class FromFile(file: File) extends Single

  case class FromResource(resource: String) extends Single

  case class FromXML(xml: Node) extends Single

  case class Sequence(override val toSeq: Seq[Single]) extends CodegenConfig {
    override def isEmpty: Boolean = toSeq.isEmpty
  }


  def empty: CodegenConfig = Sequence(Seq.empty)

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

  implicit def seqToCodegenConfig[A](seq: Seq[A])(implicit ev: A => CodegenConfig): Sequence =
    Sequence(seq.flatMap(ev(_).toSeq))

  implicit def nodeBufferToCodegenConfig(buffer: NodeBuffer): Sequence =
    Sequence(buffer.map(FromXML))


  implicit val appendCodegenConfigToCodegenConfig: Append.Values[CodegenConfig, CodegenConfig] = _ ++ _

  implicit def appendSingleToCodegenConfig[A](implicit ev: A => Single): Append.Value[CodegenConfig, A] = _ + _

  implicit def appendSequenceToCodegenConfig[A](implicit ev: A => Sequence): Append.Values[CodegenConfig, A] = _ ++ _

}
