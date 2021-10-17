package sbtjooq.codegen.internal

import sbt._
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
