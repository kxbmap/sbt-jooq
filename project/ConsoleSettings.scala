import sbt.Keys._
import sbt._

object ConsoleSettings extends AutoPlugin {

  override def trigger: PluginTrigger = allRequirements

  override def projectSettings: Seq[Setting[_]] =
    Seq(Compile, Test).map(_ / console / scalacOptions += "-Xlint:-unused")

}
