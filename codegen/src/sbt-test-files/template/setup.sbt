import java.sql.DriverManager
import org.h2.tools.RunScript
import sbt.io.Using

TaskKey[Unit]("setup") := {
  Class.forName("org.h2.Driver")
  Using.resource(DriverManager.getConnection)("jdbc:h2:./test") { conn =>
    IO.reader(file("setup.sql"))(RunScript.execute(conn, _))
  }
}
