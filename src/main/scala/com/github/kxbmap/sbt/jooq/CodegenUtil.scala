package com.github.kxbmap.sbt.jooq

import scala.xml.transform.RewriteRule

object CodegenUtil {

  def isJigsawEnabled(javaVersion: String): Boolean =
    javaVersion.takeWhile(_.isDigit).toInt >= 9


  def rewriteRule(name0: String)(f: PartialFunction[xml.Node, Seq[xml.Node]]): RewriteRule = new RewriteRule {
    override val name: String = name0
    override def transform(n: xml.Node): Seq[xml.Node] = f.applyOrElse(n, (_: xml.Node) => n)
  }

}
