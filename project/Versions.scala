import sbt.librarymanagement.VersionNumber

object Versions {

  val scalaVersion_ = "2.13.3"

  val jooqVersion = "3.13.6"

  val fastParseVersion = "2.3.0"

  val h2Version = "1.4.200"

  val sbtSlf4jSimpleVersion = "0.2.0"

  val sbtWartRemoverVersion = "2.4.12"


  def minorVersion(version: String): String =
    version match {
      case VersionNumber(x +: y +: _, _, _) => s"$x.$y"
      case _ => version
    }

}
