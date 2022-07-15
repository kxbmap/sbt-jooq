import sbt.librarymanagement.VersionNumber

object Versions {

  final val scala212 = "2.12.16"
  final val scala213 = "2.13.8"
  final val scala3 = "3.1.3"

  final val jooqVersion = "3.17.2"

  val jooqVersions: Seq[String] = Seq(
    jooqVersion,
    "3.16.8",
    "3.15.12",
    "3.14.16",
    "3.13.6",
    "3.12.4",
    "3.11.12",
    "3.10.8"
  )

  final val jetbrainsAnnotationsVersion = "23.0.0"

  final val scalaXMLVersion = "2.1.0"

  final val logbackVersion = "1.2.11"

  final val h2Version = "2.1.214"

  final val h2V1Version = "1.4.200"

  final val wartRemoverVersion = "3.0.5"

  final val javaxActivationVersion = "1.1.1"

  final val jaxbApiVersion = "2.3.1"

  final val jaxbCoreVersion = "2.3.0.1"

  final val jaxbImplVersion = "2.3.1"

  final val javaxAnnotationApiVersion = "1.3.2"

  final val flywaySbtVersion = "7.4.0"

  final val scalaTestVersion = "3.2.12"

  def minorVersion(version: String): String =
    version match {
      case VersionNumber(x +: y +: _, _, _) => s"$x.$y"
      case _ => version
    }

}
