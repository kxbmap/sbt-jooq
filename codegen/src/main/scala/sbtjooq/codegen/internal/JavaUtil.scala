package sbtjooq.codegen.internal

import sbt._

object JavaUtil {

  def parseJavaVersion(javaHome: File): Int = {
    val releaseFile = javaHome / "release"
    val versionLine = """JAVA_VERSION="(.+)"""".r
    IO.readLines(releaseFile).collectFirst {
      case versionLine(v) => majorVersion(v)
    }.getOrElse {
      sys.error(s"Cannot parse JAVA_VERSION in $javaHome")
    }
  }

  def majorVersion(full: String): Int =
    full.stripPrefix("1.").takeWhile(_.isDigit).toInt

  def isJigsawEnabled(javaVersion: Int): Boolean = javaVersion >= 9

  def isJavaEEModulesBundled(javaVersion: Int): Boolean = javaVersion <= 10

}
