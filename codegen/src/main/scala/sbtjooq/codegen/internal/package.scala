package sbtjooq.codegen

import sbt._
import sbt.JavaVersion
import sbtjooq.JooqVersion

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

  implicit class JooqVersionOps(jooqVersion: JooqVersion) {
    def needsJaxbSettings: Boolean =
      jooqVersion.matches("<=3.11")

    def generatedAnnotationDisabledByDefault: Boolean =
      jooqVersion.matches(">=3.13")
  }

  type CodegenVersions = (JooqVersion, JavaVersion)

  implicit class CodegenVersionsOps(versions: CodegenVersions) {
    def jooq: JooqVersion = versions._1
    def java: JavaVersion = versions._2

    def useJavaxAnnotationByDefault: Boolean =
      java.major <= 8 || jooq.matches("<3.12")
  }

}
