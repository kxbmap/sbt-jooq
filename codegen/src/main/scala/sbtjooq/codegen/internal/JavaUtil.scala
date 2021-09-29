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
    (if (full.startsWith("1.")) full.drop(2) else full).takeWhile(_.isDigit).toInt

  def isJigsawEnabled(javaVersion: Int): Boolean = javaVersion >= 9

  def isJavaEEModulesBundled(javaVersion: Int): Boolean = javaVersion <= 10

}
