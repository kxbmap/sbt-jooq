/*
 * Copyright 2015 Tsukasa Kitachi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
