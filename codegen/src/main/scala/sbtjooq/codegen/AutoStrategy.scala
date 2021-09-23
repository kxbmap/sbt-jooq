package sbtjooq.codegen

sealed trait AutoStrategy

object AutoStrategy {

  case object Always extends AutoStrategy

  case object IfAbsent extends AutoStrategy

  case object Never extends AutoStrategy

}
