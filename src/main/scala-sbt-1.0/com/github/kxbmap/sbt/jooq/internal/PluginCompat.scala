package com.github.kxbmap.sbt.jooq.internal

object PluginCompat {

  type Using[Source, T] = sbt.io.Using[Source, T]

  val Using = sbt.io.Using

}
