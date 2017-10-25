package com.github.kxbmap.sbt.jooq

object PluginCompat {

  def none[A]: Option[A] = None

  implicit class OptionIdOps[A](val a: A) extends AnyVal {
    def some: Option[A] = Some(a)
  }

}
