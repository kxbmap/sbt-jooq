import sbt.*
import sbt.Keys.*
import ProjectUtil.*

object ScalacOptions extends AutoPlugin {

  override def trigger: PluginTrigger = allRequirements

  override def projectSettings: Seq[Setting[?]] = Seq(
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
