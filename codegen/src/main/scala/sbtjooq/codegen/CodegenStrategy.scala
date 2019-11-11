package sbtjooq.codegen

sealed trait CodegenStrategy

object CodegenStrategy {

  case object Always extends CodegenStrategy

  case object IfAbsent extends CodegenStrategy

  case object Never extends CodegenStrategy

}
