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

package sbtjooq

import sbt.*

trait JooqKeys {

  val jooqVersion = settingKey[JooqVersion]("jOOQ version")
  val jooqOrganization = settingKey[String]("jOOQ organization/group ID")
  val jooqModules = settingKey[Seq[String]]("jOOQ modules")
  val autoJooqLibrary = settingKey[Boolean]("Add jOOQ dependencies if true")

  val jetbrainsAnnotationsVersion = settingKey[String]("Jetbrains Annotations version")

}

object JooqKeys extends JooqKeys
