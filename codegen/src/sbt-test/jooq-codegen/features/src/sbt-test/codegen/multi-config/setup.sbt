import java.sql.DriverManager
import org.h2.tools.RunScript
import sbt.io.Using

TaskKey[Unit]("setup") := {
  Class.forName("org.h2.Driver")
  Seq("setup.sql", "setup1.sql").zipWithIndex.foreach {
    case (s, n) =>
      Using.resource(DriverManager.getConnection)(s"jdbc:h2:./db$n") { conn =>
        IO.reader(file(s))(RunScript.execute(conn, _))
      }
  }
}
