import sbt._
import sbt.Keys._
import ProjectUtil._

object ScalacOptions extends AutoPlugin {

  override def trigger: PluginTrigger = allRequirements

  override def projectSettings: Seq[Setting[_]] = Seq(
    scalacOptions ++= Seq(
      "-release",
      "8",
      "-deprecation",
      "-unchecked",
      "-feature"
    ) ++ partialVersionSeq(scalaVersion.value) {
      case (2, _) => Seq("-Xlint")
    }
  )

}
