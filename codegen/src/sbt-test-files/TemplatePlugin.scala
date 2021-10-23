import java.nio.file.{Files, Path}
import java.util.stream.Collectors._
import java.util.stream.Stream
import sbt._
import sbt.Keys._
import sbt.ScriptedPlugin.autoImport._
import sbt.io.Using
import sbt.plugins.SbtPlugin
import scala.collection.JavaConverters._

object TemplatePlugin extends AutoPlugin {

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

  implicit class StreamOps[A](s: Stream[A]) {
    def mapL[B](f: A => B): List[B] = s.map[B](f(_)).collect(toList()).asScala.toList
  }

  private val walk = Using.resource(Files.walk(_: Path))

  private def listFiles(dir: File): List[(File, File)] =
    if (!dir.exists()) Nil
    else {
      val base = dir.toPath
      walk(base)(_.filter(Files.isRegularFile(_)).mapL(p => (p.toFile, base.relativize(p).toFile)))
    }

  private val listDir = Using.resource(Files.list(_: Path).filter(Files.isDirectory(_)))

  private def listTests(dir: File): List[File] =
    listDir(dir.toPath)(_.mapL(listDir(_)(_.mapL(_.toFile))).flatten)

  private def readSet(f: File): Set[File] = {
    if (f.exists())
      IO.readLines(f).map(file).toSet
    else
      Set.empty
  }

  private def includes(f: File, inc: Set[File], exc: Set[File]): Boolean =
    (inc.isEmpty || inc(f)) && !exc(f)

}
