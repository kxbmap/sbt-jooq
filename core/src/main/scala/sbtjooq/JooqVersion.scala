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

import sbt.librarymanagement.{DependencyBuilders, ModuleID, SemanticSelector, VersionNumber}
import scala.language.implicitConversions

final class JooqVersion private (
    private val version: VersionNumber
) {

  def numbers: Seq[Long] = version.numbers

  def matches(semanticSelector: String): Boolean =
    version.matchesSemVer(SemanticSelector(semanticSelector))

  override def toString: String = version.toString

  override def hashCode(): Int = version.hashCode()

  override def equals(that: Any): Boolean = that match {
    case v: JooqVersion => version == v.version
    case _ => false
  }
}

object JooqVersion {

  def apply(version: String): JooqVersion =
    new JooqVersion(VersionNumber(version))

  implicit def stringToJooqVersion(version: String): JooqVersion =
    JooqVersion(version)

  implicit class OrganizationArtifactNameOps(oan: DependencyBuilders.OrganizationArtifactName) {
    def %(revision: JooqVersion): ModuleID =
      oan % revision.toString
  }

}
