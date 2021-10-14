package sbtjooq.codegen

import sbt._
import sbt.JavaVersion

package object internal {

  def parseJavaVersion(javaHome: File): JavaVersion = {
    val releaseFile = javaHome / "release"
    val versionLine = """JAVA_VERSION="(.+)"""".r
    IO.readLines(releaseFile).collectFirst {
      case versionLine(v) => JavaVersion(v)
    }.getOrElse {
      sys.error(s"Cannot parse JAVA_VERSION in $javaHome")
    }
  }

  implicit class JavaVersionOps(javaVersion: JavaVersion) {
    def major: Long = javaVersion.numbers match {
      case Vector(1L, x, _*) => x
      case Vector(x, _*) => x
    }

    def isJigsawEnabled: Boolean = major >= 9

    def isJavaEEModulesBundled: Boolean = major <= 10
  }

}
