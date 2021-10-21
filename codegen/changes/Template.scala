import java.nio.file.{Files, Path}
import java.util.stream.Collectors._
import java.util.stream.Stream
import sbt._
import sbt.Keys._
import sbt.ScriptedPlugin.autoImport._
import sbt.plugins.SbtPlugin
import scala.collection.JavaConverters._

object Template extends AutoPlugin {

  override def requires: Plugins = SbtPlugin

  override def trigger: PluginTrigger = allRequirements

  object autoImport {
    val copyTemplate = taskKey[Unit]("")
  }

  import autoImport._

  override def projectSettings: Seq[Setting[_]] = Seq(
    copyTemplate := copyTeamplateTask.value,
    scripted := scripted.dependsOn(copyTemplate).evaluated,
  )

  private def copyTeamplateTask: Def.Initialize[Task[Unit]] = Def.task {
    val files = Seq(
      baseDirectory.value / "template",
      file(sys.props("scripted.template.dir")),
    ).flatMap(listFiles)
    val copies = for {
      test <- listTests(sbtTestDirectory.value)
      inc = readSet(test / "includes")
      exc = readSet(test / "excludes")
      (src, rel) <- files if includes(rel, inc, exc)
      dst = IO.resolve(test, rel) if !dst.exists()
    } yield src -> dst
    IO.copy(copies)
  }

  private def listFiles(dir: File): List[(File, File)] = {
    def traverse(path: Path): Stream[File] =
      Files.list(path).flatMap[File] {
        case p if Files.isDirectory(p) => traverse(p)
        case p => Stream.of(Seq(p.toFile): _*)
      }
    if (!dir.exists()) Nil
    else
      traverse(dir.toPath).collect(toList()).asScala
        .flatMap(f => IO.relativizeFile(dir, f).map(f -> _))
        .toList
  }

  private def listTests(dir: File): List[File] =
    Files.list(dir.toPath)
      .filter(Files.isDirectory(_))
      .flatMap[Path](Files.list(_))
      .filter(Files.isDirectory(_))
      .map[File](_.toFile)
      .collect(toList()).asScala.toList

  private def readSet(f: File): Set[File] = {
    if (f.exists())
      IO.readLines(f).map(file).toSet
    else
      Set.empty
  }

  private def includes(f: File, inc: Set[File], exc: Set[File]): Boolean =
    (inc.isEmpty || inc(f)) && !exc(f)

}
