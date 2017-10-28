package com.github.kxbmap.sbt.jooq

import sbt._
import scala.xml.transform.RewriteRule

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


  def rewriteRule(name0: String)(f: PartialFunction[xml.Node, Seq[xml.Node]]): RewriteRule = new RewriteRule {
    override val name: String = name0
    override def transform(n: xml.Node): Seq[xml.Node] = f.applyOrElse(n, (_: xml.Node) => n)
  }

}
