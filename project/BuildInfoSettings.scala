import sbt.Keys._
import sbt._
import sbtbuildinfo.BuildInfoKeys._
import sbtbuildinfo.{BuildInfoOption, BuildInfoPlugin}

object BuildInfoSettings extends AutoPlugin {

  override def requires: Plugins = BuildInfoPlugin

  override def trigger: PluginTrigger = allRequirements

  override def projectSettings: Seq[Setting[_]] = Seq(
    buildInfoOptions ++= Seq(BuildInfoOption.PackagePrivate, BuildInfoOption.ConstantValue),
    buildInfoUsePackageAsPath := true,
    Compile / packageSrc / mappings += {
      val pkg = buildInfoPackage.value.replaceAll("""\.""", "/")
      val src = s"${buildInfoObject.value}.scala"
      (Compile / sourceManaged).value / pkg / src -> (file(pkg) / src).toString
    },
    Compile / packageBin / mappings := {
      val pkg = buildInfoPackage.value.replaceAll("""\.""", "/")
      val obj = buildInfoObject.value
      val excludes = Set(s"$obj.class", s"$obj$$.class").map(file(pkg) / _).map(_.toString)
      (Compile / packageBin / mappings).value.filter {
        case (_, x) => !excludes(x)
      }
    }
  )

}
