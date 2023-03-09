import sbt.Keys.*
import sbt.*

object ConsoleSettings extends AutoPlugin {

  override def trigger: PluginTrigger = allRequirements

  override def projectSettings: Seq[Setting[?]] =
    Seq(Compile, Test).map(_ / console / scalacOptions += "-Xlint:-unused")

}
