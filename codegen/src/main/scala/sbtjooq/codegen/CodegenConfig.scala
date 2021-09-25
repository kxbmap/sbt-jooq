package sbtjooq.codegen

import scala.language.implicitConversions
import scala.xml.Node

sealed trait CodegenConfig {
  def toSeq: Seq[CodegenConfig.Single]
  def isEmpty: Boolean = false
}

object CodegenConfig {

  sealed trait Single extends CodegenConfig  {
    override def toSeq: Seq[Single] = Seq(this)
  }

  case class File(file: sbt.File) extends Single

  case class Resource(resource: String) extends Single

  case class XML(xml: Node) extends Single

  case class Sequence(seq: Seq[CodegenConfig]) extends CodegenConfig {
    override def toSeq: Seq[Single] = seq.flatMap(_.toSeq)
    override def isEmpty: Boolean = seq.forall(_.isEmpty)
  }


  def empty: CodegenConfig = Sequence(Seq.empty)

  def fromURI(uri: sbt.URI): CodegenConfig =
    uri.getScheme match {
      case "classpath" => Resource(uri.getSchemeSpecificPart)
      case "file" => File(new sbt.File(uri))
      case _ => throw new IllegalArgumentException(s"Unknown scheme: $uri")
    }

  def fromURIString(uri: String): CodegenConfig = fromURI(sbt.uri(uri))


  implicit def fileToCodegenConfig(file: sbt.File): CodegenConfig = File(file)

  implicit def xmlNodeToCodegenConfig(xml: Node): CodegenConfig = XML(xml)

  implicit def uriToCodegenConfig(uri: sbt.URI): CodegenConfig = fromURI(uri)

  implicit def seqToCodegenConfig(seq: Seq[CodegenConfig]): CodegenConfig = Sequence(seq)

}
