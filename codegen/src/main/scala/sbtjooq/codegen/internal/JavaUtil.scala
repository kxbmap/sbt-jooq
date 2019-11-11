package sbtjooq.codegen.internal

import sbt._

object JavaUtil {

  def parseJavaVersion(javaHome: File): String = {
    val releaseFile = javaHome / "release"
    val versionLine = """JAVA_VERSION="(.+)"""".r
    IO.readLines(releaseFile).collectFirst {
      case versionLine(version) => version
    }.getOrElse {
      sys.error(s"Cannot parse JAVA_VERSION in $javaHome")
    }
  }

  private def major(javaVersion: String): Int =
    javaVersion.takeWhile(_.isDigit).toInt

  def isJigsawEnabled(javaVersion: String): Boolean =
    major(javaVersion) >= 9

  def isJAXBBundled(javaVersion: String): Boolean =
    major(javaVersion) <= 10

  def isJavaxAnnotationBundled(javaVersion: String): Boolean =
    major(javaVersion) <= 10

}
