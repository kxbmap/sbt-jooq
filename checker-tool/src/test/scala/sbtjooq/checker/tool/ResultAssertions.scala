package sbtjooq.checker.tool

import org.scalatest.{Assertion, Assertions}
import org.wartremover.test.WartTestTraverser

/** Copied from WartRemover [[https://github.com/wartremover/wartremover]]
  */
trait ResultAssertions { this: Assertions =>

  def assertEmpty(result: WartTestTraverser.Result): Assertion = {
    assertResult(Nil, "result.errors")(result.errors)
    assertResult(Nil, "result.warnings")(result.warnings)
  }

  def assertError(result: WartTestTraverser.Result)(message: String): Assertion =
    assertErrors(result)(message)

  def assertErrors(result: WartTestTraverser.Result)(messages: String*): Assertion = {
    assertResult(messages, "result.errors")(result.errors.map(skipTraverserPrefix))
    assertResult(Nil, "result.warnings")(result.warnings.map(skipTraverserPrefix))
  }

  private val messageFormat = """^\[wartremover:\S+] (.+)$""".r

  private def skipTraverserPrefix(msg: String) = msg match {
    case messageFormat(rest) => rest
    case s => s
  }
}
