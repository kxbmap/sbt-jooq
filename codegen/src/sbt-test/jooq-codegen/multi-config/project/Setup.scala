import java.sql.DriverManager
import org.h2.tools.RunScript
import sbt.Keys._
import sbt._
import scala.util.Using

object Setup extends AutoPlugin {

  override def trigger: PluginTrigger = allRequirements

  object autoImport {
    val setup = taskKey[Unit]("Setup databse")
  }

  import autoImport._

  override def globalSettings: Seq[Setting[_]] = Seq(
    setup := setupTask.value,
  )

  private def setupTask = Def.task {
    Class.forName("org.h2.Driver")
    (1 to 2).foreach { n =>
      Using.resource(DriverManager.getConnection(s"jdbc:h2:./db$n")) { conn =>
        IO.reader(file(s"setup$n.sql")) {
          RunScript.execute(conn, _)
        }
      }
    }
  }

}
