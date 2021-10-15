package sbtjooq

import sbt.librarymanagement.{DependencyBuilders, ModuleID, SemanticSelector, VersionNumber}
import scala.language.implicitConversions

final class JooqVersion private(
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
