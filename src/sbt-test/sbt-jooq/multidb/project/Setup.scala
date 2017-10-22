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
    setup := setupTask.value
  )

  private def setupTask = Def.task {
    Class.forName("org.h2.Driver")
    Seq("db1", "db2").foreach { db =>
      val conn = DriverManager.getConnection(s"jdbc:h2:./$db")
      try
        IO.reader(file(s"setup/$db.sql")) {
          RunScript.execute(conn, _)
        }
      finally
        conn.close()
    }
  }

}
