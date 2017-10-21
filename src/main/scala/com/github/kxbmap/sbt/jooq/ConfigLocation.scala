package com.github.kxbmap.sbt.jooq

import scala.language.implicitConversions

sealed trait ConfigLocation

object ConfigLocation {

  case class File(file: sbt.File) extends ConfigLocation

  case class Classpath(resource: String) extends ConfigLocation


  trait Implicits {

    implicit def fileToConfigLocation(file: sbt.File): File = File(file)

    implicit class ConfigLocationInterpolation(sc: StringContext) {

      def file(args: Any*): File = File(sbt.file(sc.s(args: _*)))

      def classpath(args: Any*): Classpath = Classpath(sc.s(args: _*))

      def cp(args: Any*): Classpath = classpath(args: _*)

    }

  }

}
