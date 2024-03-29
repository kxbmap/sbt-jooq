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

package sbtjooq.checker

import sbt.*

trait JooqCheckerKeys {

  val JooqChecker = config("jooq-checker").hide

  val jooqCheckerLevelPlainSQL = settingKey[JooqCheckerLevel]("Error level of PlainSQL checker")
  val jooqCheckerLevelSQLDialect = settingKey[JooqCheckerLevel]("Error level of SQLDialect checker")

}

object JooqCheckerKeys extends JooqCheckerKeys
