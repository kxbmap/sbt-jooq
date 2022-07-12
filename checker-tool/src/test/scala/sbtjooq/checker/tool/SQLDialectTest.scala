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

import org.jooq.{Allow, Require, SQL, Support}
import org.jooq.SQLDialect.*
import org.wartremover.test.WartTestTraverser

//noinspection NotImplementedCode,ScalaUnusedSymbol
class SQLDialectTest extends UnitSpec {

  @Support
  def supportAll: SQL = ???

  @Support(Array(MYSQL))
  def supportMySQL: SQL = ???

  @Support(Array(POSTGRES))
  def supportPostgres: SQL = ???

  @Support(Array(H2))
  def supportH2: SQL = ???

  @Support(Array(MYSQL, POSTGRES))
  def supportMySQLAndPostgres: SQL = ???

  "SQLDialect wart" when {

    "in no @Allow scope" should {

      "be error to call any @Support API" in {
        val result = WartTestTraverser(SQLDialect) {
          val all = supportAll
          val my: SQL = supportMySQL
          val pg: SQL = supportPostgres
          val h2: SQL = supportH2
          val myAndPg: SQL = supportMySQLAndPostgres
        }
        assertErrors(result)(Seq.fill(5)("No jOOQ API usage is allowed at current scope. Use @Allow.")*)
      }

    }

    "in @Allow scope" should {

      "be @Support API callable" in {
        val result = WartTestTraverser(SQLDialect) {
          @Allow
          val allow: SQL = supportAll
        }
        assertEmpty(result)
      }

      "be @Support(XXX) API callable" in {
        val result = WartTestTraverser(SQLDialect) {
          @Allow
          class AllowAll {
            val my: SQL = supportMySQL
            val pg: SQL = supportPostgres
            val h2: SQL = supportH2
          }
        }
        assertEmpty(result)
      }

      "be @Support(XXX, YYY) API callable" in {
        val result = WartTestTraverser(SQLDialect) {
          @Allow
          class AllowAll {
            val myAndPg: SQL = supportMySQLAndPostgres
          }
        }
        assertEmpty(result)
      }

    }

    "in @Allow(XXX) scope" should {

      "be @Support API callable" in {
        val result = WartTestTraverser(SQLDialect) {
          @Allow(Array(MYSQL))
          class AllowMySQL {
            val all: SQL = supportAll
          }
        }
        assertEmpty(result)
      }

      "be @Support(XXX) API callable" in {
        val result = WartTestTraverser(SQLDialect) {
          @Allow(Array(MYSQL))
          class AllowMySQL {
            val my: SQL = supportMySQL
          }
        }
        assertEmpty(result)
      }

      "be error to call @Support(YYY) API" in {
        val result = WartTestTraverser(SQLDialect) {
          @Allow(Array(MYSQL))
          class AllowMySQL {
            val pg: SQL = supportPostgres
            val h2: SQL = supportH2
          }
        }
        assertErrors(result)(
          "The allowed dialects in scope [MYSQL] do not include any of the supported dialects: [POSTGRES]",
          "The allowed dialects in scope [MYSQL] do not include any of the supported dialects: [H2]",
        )
      }

      "be @Support(XXX, YYY) API callable" in {
        val result = WartTestTraverser(SQLDialect) {
          @Allow(Array(MYSQL))
          class AllowMySQL {
            val myAndPg: SQL = supportMySQLAndPostgres
          }
        }
        assertEmpty(result)
      }

    }

    "in @Allow(XXX, YYY) scope" should {

      "be @Support API callable" in {
        val result = WartTestTraverser(SQLDialect) {
          @Allow(Array(MYSQL, POSTGRES))
          class AllowMySQLAndPostgres {
            val all: SQL = supportAll
          }
        }
        assertEmpty(result)
      }

      "be @Support(XXX) API callable" in {
        val result = WartTestTraverser(SQLDialect) {
          @Allow(Array(MYSQL, POSTGRES))
          class AllowMySQLAndPostgres {
            val my: SQL = supportMySQL
          }
        }
        assertEmpty(result)
      }

      "be @Support(YYY) API callable" in {
        val result = WartTestTraverser(SQLDialect) {
          @Allow(Array(MYSQL, POSTGRES))
          class AllowMySQLAndPostgres {
            val pg: SQL = supportPostgres
          }
        }
        assertEmpty(result)
      }

      "be @Support(XXX, YYY) API callable" in {
        val result = WartTestTraverser(SQLDialect) {
          @Allow(Array(MYSQL, POSTGRES))
          class AllowMySQLAndPostgres {
            val myAndPg: SQL = supportMySQLAndPostgres
          }
        }
        assertEmpty(result)
      }

      "be error to call @Support(ZZZ) API" in {
        val result = WartTestTraverser(SQLDialect) {
          @Allow(Array(MYSQL, POSTGRES))
          class AllowMySQLAndPostgres {
            val h2: SQL = supportH2
          }
        }
        assertError(result)(
          "The allowed dialects in scope [MYSQL, POSTGRES] do not include any of the supported dialects: [H2]"
        )
      }

    }

    "in nested @Allow(XXX) and @Allow(YYY) scope" should {

      "be @Support API callable" in {
        val result = WartTestTraverser(SQLDialect) {
          @Allow(Array(MYSQL))
          class AllowMySQL {
            @Allow(Array(POSTGRES))
            def allowPostgres: SQL = supportAll
          }
        }
        assertEmpty(result)
      }

      "be @Support(XXX) API callable" in {
        val result = WartTestTraverser(SQLDialect) {
          @Allow(Array(MYSQL))
          class AllowMySQL {
            @Allow(Array(POSTGRES))
            def allowPostgres: SQL = supportMySQL
          }
        }
        assertEmpty(result)
      }

      "be @Support(YYY) API callable" in {
        val result = WartTestTraverser(SQLDialect) {
          @Allow(Array(MYSQL))
          class AllowMySQL {
            @Allow(Array(POSTGRES))
            def allowPostgres: SQL = supportPostgres
          }
        }
        assertEmpty(result)
      }

      "be @Support(XXX, YYY) API callable" in {
        val result = WartTestTraverser(SQLDialect) {
          @Allow(Array(MYSQL))
          class AllowMySQL {
            @Allow(Array(POSTGRES))
            def allowPostgres: SQL = supportMySQLAndPostgres
          }
        }
        assertEmpty(result)
      }

      "be error to call @Support(ZZZ) API" in {
        val result = WartTestTraverser(SQLDialect) {
          @Allow(Array(MYSQL))
          class AllowMySQL {
            @Allow(Array(POSTGRES))
            def allowPostgres: SQL = supportH2
          }
        }
        assertError(result)(
          "The allowed dialects in scope [MYSQL, POSTGRES] do not include any of the supported dialects: [H2]"
        )
      }

    }

    "in @Require(XXX) scope" should {

      "be @Support API callable" in {
        val result = WartTestTraverser(SQLDialect) {
          @Allow
          class AllowAll {
            @Require(Array(MYSQL))
            def requireMySQL: SQL = supportAll
          }
        }
        assertEmpty(result)
      }

      "be @Support(XXX) API callable" in {
        val result = WartTestTraverser(SQLDialect) {
          @Allow
          class AllowAll {
            @Require(Array(MYSQL))
            def requireMySQL: SQL = supportMySQL
          }
        }
        assertEmpty(result)
      }

      "be error to call @Support(YYY) API" in {
        val result = WartTestTraverser(SQLDialect) {
          @Allow
          class AllowAll {
            @Require(Array(MYSQL))
            def requireMySQL: SQL = supportPostgres
          }
        }
        assertError(result)("Not all of the required dialects [MYSQL] from the current scope are supported [POSTGRES]")
      }

      "be @Support(XXX, YYY) API callable" in {
        val result = WartTestTraverser(SQLDialect) {
          @Allow
          class AllowAll {
            @Require(Array(MYSQL))
            def requireMySQL: SQL = supportMySQLAndPostgres
          }
        }
        assertEmpty(result)
      }

    }

    "in @Require(XXX, YYY) scope" should {

      "be @Support API callable" in {
        val result = WartTestTraverser(SQLDialect) {
          @Allow
          class AllowAll {
            @Require(Array(MYSQL, POSTGRES))
            def requireMySQLAndPostgres: SQL = supportAll
          }
        }
        assertEmpty(result)
      }

      "be error to call @Support(XXX) API" in {
        val result = WartTestTraverser(SQLDialect) {
          @Allow
          class AllowAll {
            @Require(Array(MYSQL, POSTGRES))
            def requireMySQLAndPostgres: SQL = supportMySQL
          }
        }
        assertError(result)(
          "Not all of the required dialects [MYSQL, POSTGRES] from the current scope are supported [MYSQL]"
        )
      }

      "be error to call @Support(YYY) API" in {
        val result = WartTestTraverser(SQLDialect) {
          @Allow
          class AllowAll {
            @Require(Array(MYSQL, POSTGRES))
            def requireMySQLAndPostgres: SQL = supportPostgres
          }
        }
        assertError(result)(
          "Not all of the required dialects [MYSQL, POSTGRES] from the current scope are supported [POSTGRES]"
        )
      }

      "be @Support(XXX, YYY) API callable" in {
        val result = WartTestTraverser(SQLDialect) {
          @Allow
          class AllowAll {
            @Require(Array(MYSQL, POSTGRES))
            def requireMySQLAndPostgres: SQL = supportMySQLAndPostgres
          }
        }
        assertEmpty(result)
      }

    }

    "in nested @Require(XXX) and @Require(YYY) scope" should {

      "enables only inner most one" should {

        "be @Support API callable" in {
          val result = WartTestTraverser(SQLDialect) {
            @Allow
            class AllowAll {
              @Require(Array(MYSQL))
              class RequireMySQL {
                @Require(Array(POSTGRES))
                def requirePostgres: SQL = supportAll
              }
            }
          }
          assertEmpty(result)
        }

        "be error to call @Support(XXX) API" in {
          val result = WartTestTraverser(SQLDialect) {
            @Allow
            class AllowAll {
              @Require(Array(MYSQL))
              class RequireMySQL {
                @Require(Array(POSTGRES))
                def requirePostgres: SQL = supportMySQL
              }
            }
          }
          assertError(result)(
            "Not all of the required dialects [POSTGRES] from the current scope are supported [MYSQL]"
          )
        }

        "be @Support(YYY) API callable" in {
          val result = WartTestTraverser(SQLDialect) {
            @Allow
            class AllowAll {
              @Require(Array(MYSQL))
              class RequireMySQL {
                @Require(Array(POSTGRES))
                def requirePostgres: SQL = supportPostgres
              }
            }
          }
          assertEmpty(result)
        }

        "be @Support(XXX, YYY) API callable" in {
          val result = WartTestTraverser(SQLDialect) {
            @Allow
            class AllowAll {
              @Require(Array(MYSQL))
              class RequireMySQL {
                @Require(Array(POSTGRES))
                def requirePostgres: SQL = supportMySQLAndPostgres
              }
            }
          }
          assertEmpty(result)
        }

      }

    }

    "in @SuppressWarnings scope" should {

      "suppress @Allow errors" in {
        val result = WartTestTraverser(SQLDialect) {
          @Allow(Array(MYSQL))
          @SuppressWarnings(Array("sbtjooq.checker.tool.SQLDialect"))
          val suppressed1 = supportPostgres
        }
        assertEmpty(result)
      }

      "suppress @Require errors" in {
        val result = WartTestTraverser(SQLDialect) {
          @Allow
          @Require(Array(MYSQL))
          @SuppressWarnings(Array("sbtjooq.checker.tool.SQLDialect"))
          val suppressed2 = supportPostgres
        }
        assertEmpty(result)
      }

    }

  }

}
