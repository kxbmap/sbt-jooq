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

import org.jooq.{Allow, SQL}
import org.wartremover.test.WartTestTraverser

//noinspection NotImplementedCode,ScalaUnusedSymbol
class PlainSQLTest extends UnitSpec {

  @org.jooq.PlainSQL
  def plainSQL: SQL = ???

  "PlainSQL wart" when {

    "in no @Allow.PlainSQL scope" should {

      "be error to call @PlainSQL API" in {
        val result = WartTestTraverser(PlainSQL) {
          val x = plainSQL
        }
        assertError(result)("Plain SQL usage not allowed at current scope. Use @Allow.PlainSQL.")
      }

    }

    "in @Allow.PlainSQL scope" should {

      "be @PlainSQL API callable" in {
        val result = WartTestTraverser(PlainSQL) {
          @Allow.PlainSQL
          val allowedVal = plainSQL
          @Allow.PlainSQL
          def allowedDef = plainSQL
          @Allow.PlainSQL
          class AllowedClass {
            def sql: SQL = plainSQL
          }
          @Allow.PlainSQL
          object AllowedObject {
            def sql: SQL = plainSQL
          }
        }
        assertEmpty(result)
      }

    }

    "in @SuppressWarnings scope" should {

      "suppress @PlainSQL error" in {
        val result = WartTestTraverser(PlainSQL) {
          @SuppressWarnings(Array("sbtjooq.checker.tool.PlainSQL"))
          val suppressed = plainSQL
        }
        assertEmpty(result)
      }

    }

  }

}
