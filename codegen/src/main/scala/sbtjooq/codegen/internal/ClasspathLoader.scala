package sbtjooq.codegen.internal

import java.net.URLClassLoader
import sbt._
import sbt.Def.Classpath
import sbt.io.Using

class ClasspathLoader(classpath: Classpath)
  extends URLClassLoader(classpath.files.map(_.asURL).toArray, null)

object ClasspathLoader {
  val using: Using[Classpath, ClasspathLoader] = Using.resource(new ClasspathLoader(_))
}
