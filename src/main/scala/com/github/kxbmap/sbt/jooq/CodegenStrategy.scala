package com.github.kxbmap.sbt.jooq

sealed trait CodegenStrategy

object CodegenStrategy {

  case object Always extends CodegenStrategy

  case object IfAbsent extends CodegenStrategy

}
