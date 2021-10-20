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

package sbtjooq.codegen

import org.scalactic.Prettifier
import org.scalactic.source.Position
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.Assertion
import scala.xml.Node
import scala.xml.Utility.trim

abstract class UnitSpec extends AnyWordSpec {

  def assertXML(expected: Node)(actual: Node)(implicit prettifier: Prettifier, pos: Position): Assertion =
    assertResult(trim(expected).toString())(trim(actual).toString())

}
