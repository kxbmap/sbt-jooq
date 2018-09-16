package sbtjooq.codegen

import sbt._

object CodegenUtil {

  def parseJavaVersion(javaHome: File): String = {
    val releaseFile = javaHome / "release"
    val versionLine = """JAVA_VERSION="(.+)"""".r
    IO.readLines(releaseFile).collectFirst {
      case versionLine(version) => version
    }.getOrElse {
      sys.error(s"Cannot parse JAVA_VERSION in $javaHome")
    }
  }

  def isJigsawEnabled(javaVersion: String): Boolean =
    javaVersion.takeWhile(_.isDigit).toInt >= 9

}
