package com.github.kxbmap.sbt.jooq

import scala.language.implicitConversions
import scala.xml.Node

sealed trait CodegenConfig

object CodegenConfig {

  case class File(file: sbt.File) extends CodegenConfig

  case class Classpath(resource: String) extends CodegenConfig

  case class XML(xml: Node) extends CodegenConfig


  def fromURI(uri: sbt.URI): CodegenConfig =
    uri.getScheme match {
      case "classpath" => Classpath(uri.getSchemeSpecificPart)
      case "file" => File(new sbt.File(uri))
      case _ => throw new IllegalArgumentException(s"Unknown scheme: $uri")
    }

  def fromURIString(uri: String): CodegenConfig = fromURI(sbt.uri(uri))


  trait Implicits {

    implicit def fileToCodegenConfig(file: sbt.File): File = File(file)

    implicit def xmlNodeToCodegenConfig(xml: Node): XML = XML(xml)

    implicit def uriToCodegenConfig(uri: sbt.URI): CodegenConfig = fromURI(uri)

    implicit def stringToCodegenConfig(s: String): CodegenConfig = fromURIString(s)

  }

}
