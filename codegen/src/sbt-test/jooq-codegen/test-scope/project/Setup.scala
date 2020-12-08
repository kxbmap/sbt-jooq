import java.sql.DriverManager
import org.h2.tools.RunScript
import sbt.Keys._
import sbt._

object Setup extends AutoPlugin {

  override def trigger: PluginTrigger = allRequirements

  object autoImport {
    val setup = taskKey[Unit]("Setup databse")
  }

  import autoImport._

  override def globalSettings: Seq[Setting[_]] = Seq(
    setup := setupTask.value,
//    onLoad ~= { _.andThen("setup" :: _) }
  )

  private def setupTask = Def.task {
    Class.forName("org.h2.Driver")
    val conn = DriverManager.getConnection("jdbc:h2:./test")
    try
      IO.reader(file("setup.sql")) {
        RunScript.execute(conn, _)
      }
    finally
      conn.close()
  }

}
