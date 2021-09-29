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

  private[sbtjooq] def major(javaVersion: String): Int =
    javaVersion.takeWhile(_.isDigit).toInt

  def isJigsawEnabled(javaVersion: String): Boolean =
    major(javaVersion) >= 9

  def isJavaEEModulesBundled(javaVersion: String): Boolean =
    major(javaVersion) <= 10

}
