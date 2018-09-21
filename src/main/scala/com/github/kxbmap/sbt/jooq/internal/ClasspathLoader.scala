package com.github.kxbmap.sbt.jooq.internal

import java.io.InputStream
import java.net.{URL, URLClassLoader}
import java.util.Collections
import sbt.Def.Classpath
import sbt._
import sbt.io.Using

class ClasspathLoader(classpath: Classpath) extends URLClassLoader(
  classpath.map(_.data.asURL).toArray,
  ClasspathLoader.EmptyLoader)

object ClasspathLoader {

  val using: Using[Classpath, ClasspathLoader] = Using.resource(new ClasspathLoader(_))


  private object EmptyLoader extends ClassLoader {

    override def getResource(name: String): URL = null

    override def setClassAssertionStatus(className: String, enabled: Boolean): Unit = ()

    override def clearAssertionStatus(): Unit = ()

    override def getPackage(name: String): Package = null

    override def setDefaultAssertionStatus(enabled: Boolean): Unit = ()

    override def setPackageAssertionStatus(packageName: String, enabled: Boolean): Unit = ()

    override def definePackage(
        name: String, specTitle: String, specVersion: String, specVendor: String, implTitle: String,
        implVersion: String, implVendor: String, sealBase: URL): Package =
      throw new IllegalArgumentException(name)

    override def getResources(name: String): java.util.Enumeration[URL] = Collections.emptyEnumeration()

    override def getResourceAsStream(name: String): InputStream = null

    override def loadClass(name: String): Class[_] = throw new ClassNotFoundException(name)

    override def loadClass(name: String, resolve: Boolean): Class[_] = throw new ClassNotFoundException(name)

    override def getPackages: Array[Package] = Array.empty

    override def getClassLoadingLock(className: String): AnyRef = this

  }

}
