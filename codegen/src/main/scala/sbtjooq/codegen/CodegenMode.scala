package sbtjooq.codegen

sealed trait CodegenMode

object CodegenMode {

  case object Auto extends CodegenMode

  case object Always extends CodegenMode

  case object Unmanaged extends CodegenMode

  implicit class CodegenModeOps(mode: CodegenMode) {
    def isUnmanaged: Boolean = mode == Unmanaged
  }

}
