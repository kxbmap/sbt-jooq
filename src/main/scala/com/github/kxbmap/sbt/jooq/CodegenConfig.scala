package com.github.kxbmap.sbt.jooq

import scala.language.implicitConversions
import scala.xml.Node

sealed trait CodegenConfig

object CodegenConfig {

  case class File(file: sbt.File) extends CodegenConfig

  case class Resource(resource: String) extends CodegenConfig

  case class XML(xml: Node) extends CodegenConfig


  trait Implicits {

    implicit def fileToCodegenConfig(file: sbt.File): File = File(file)

    implicit def xmlNodeToCodegenConfig(xml: Node): XML = XML(xml)

    implicit class ConfigLocationInterpolation(sc: StringContext) {

      def file(args: Any*): File = File(sbt.file(sc.s(args: _*)))

      def resource(args: Any*): Resource = Resource(sc.s(args: _*))

    }

  }

}
