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

import sbtjooq.checker.internal.JooqWarts
import wartremover.Wart

sealed trait CheckerLevel

object CheckerLevel {

  case object Disabled extends CheckerLevel

  case object Warning extends CheckerLevel

  case object Error extends CheckerLevel

}

final class CheckerLevels private (val sqlDialect: CheckerLevel, val plainSQL: CheckerLevel) {

  def withSQLDialect(level: CheckerLevel): CheckerLevels =
    new CheckerLevels(level, plainSQL)

  def withPlainSQL(level: CheckerLevel): CheckerLevels =
    new CheckerLevels(sqlDialect, level)

  private[sbtjooq] def errors: Seq[Wart] = collect(CheckerLevel.Error)

  private[sbtjooq] def warnings: Seq[Wart] = collect(CheckerLevel.Warning)

  private def collect(level: CheckerLevel): Seq[Wart] = Seq(
    if (sqlDialect == level) Seq(JooqWarts.SQLDialect) else Nil,
    if (plainSQL == level) Seq(JooqWarts.PlainSQL) else Nil,
  ).flatten

  override def toString: String = s"CheckerLevels(sqlDialect = $sqlDialect, plainSQL = $plainSQL)"
}

object CheckerLevels {

  def default: CheckerLevels = new CheckerLevels(
    sqlDialect = CheckerLevel.Error,
    plainSQL = CheckerLevel.Error,
  )

}
