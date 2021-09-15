import sbt.librarymanagement.VersionNumber

object Versions {

  val scriptedScalaVersion = "2.13.6"

  val jooqVersion = "3.15.2"

  val jooqVersionForJava8 = "3.14.14"

  val jooqVersions = Seq(
    jooqVersion,
    jooqVersionForJava8,
    "3.13.6",
    "3.12.4",
    "3.11.12",
    "3.10.8"
  )

  val fastParseVersion = "2.3.3"

  val h2Version = "1.4.200"

  val sbtSlf4jSimpleVersion = "0.2.0"

  val sbtWartRemoverVersion = "2.4.16"

  val jooqWartsVersion = "0.1.2"

  val javaxActivationVersion = "1.1.1"

  val jaxbApiVersion = "2.3.1"

  val jaxbCoreVersion = "2.3.0.1"

  val jaxbImplVersion = "2.3.1"

  val javaxAnnotationApiVersion = "1.3.2"

  val flywaySbtVersion = "7.4.0"


  def minorVersion(version: String): String =
    version match {
      case VersionNumber(x +: y +: _, _, _) => s"$x.$y"
      case _ => version
    }

}
