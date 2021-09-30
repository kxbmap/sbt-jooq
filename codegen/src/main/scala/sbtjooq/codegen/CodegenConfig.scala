package sbtjooq.codegen

import sbt._
import scala.language.implicitConversions
import scala.xml.Node

sealed trait CodegenConfig {
  def toSeq: Seq[CodegenConfig.Single]
  def isEmpty: Boolean
}

object CodegenConfig {

  sealed trait Single extends CodegenConfig {
    override def toSeq: Seq[Single] = Seq(this)
    override def isEmpty: Boolean = false
  }

  case class FromFile(file: File) extends Single

  case class FromResource(resource: String) extends Single

  case class FromXML(xml: Node) extends Single

  case class Sequence(seq: Seq[CodegenConfig]) extends CodegenConfig {
    override def toSeq: Seq[Single] = seq.flatMap(_.toSeq)
    override def isEmpty: Boolean = seq.forall(_.isEmpty)
  }


  def empty: CodegenConfig = Sequence(Seq.empty)

  def fromURI(uri: URI): CodegenConfig =
    uri.getScheme match {
      case "classpath" => FromResource(uri.getSchemeSpecificPart)
      case "file" => FromFile(new File(uri))
      case _ => throw new IllegalArgumentException(s"Unknown scheme: $uri")
    }

  def fromURIString(uri: String): CodegenConfig = fromURI(sbt.uri(uri))


  implicit def fileToCodegenConfig(file: File): CodegenConfig = FromFile(file)

  implicit def xmlNodeToCodegenConfig(xml: Node): CodegenConfig = FromXML(xml)

  implicit def uriToCodegenConfig(uri: URI): CodegenConfig = fromURI(uri)

  implicit def seqToCodegenConfig(seq: Seq[CodegenConfig]): CodegenConfig = Sequence(seq)

}
