package com.github.kxbmap.sbt.jooq

import sbt.Attributed.data
import sbt.Keys._
import sbt._
import sbt.plugins.JvmPlugin
import scala.xml.transform.{RewriteRule, RuleTransformer}
import scala.xml.{Elem, XML}

object JooqCodegen extends AutoPlugin {

  val DefaultJooqVersion = "3.10.0"

  override def requires: Plugins = JvmPlugin

  override def projectSettings: Seq[Setting[_]] = jooqCodegenSettings

  object autoImport {

    val Jooq = config("jooq").hide

    val jooqVersion = settingKey[String]("jOOQ version")
    val jooqCodegen = taskKey[Seq[File]]("Run jOOQ codegen")
    val jooqCodegenConfigFile = settingKey[Option[File]]("jOOQ codegen configuration file")
    val jooqCodegenTargetDirectory = settingKey[File]("jOOQ codegen target directory")
    val jooqCodegenConfigRewriteRules = settingKey[Seq[RewriteRule]]("jOOQ codegen configuration rewrite rules")
    val jooqCodegenConfig = taskKey[xml.Node]("jOOQ codegen configuration")
    val jooqCodegenStrategy = settingKey[CodegenStrategy]("jOOQ codegen strategy")

    val autoJooqLibrary = settingKey[Boolean]("Add jOOQ dependencies if true")

    val CodegenStrategy = com.github.kxbmap.sbt.jooq.CodegenStrategy

  }

  import autoImport.{CodegenStrategy => _, _}

  private val forkOptions = taskKey[ForkOptions]("fork options")
  private val forkJavaVersion = taskKey[String]("fork Java version")

  private lazy val jooqCodegenSettings: Seq[Setting[_]] = Seq(
    jooqVersion := DefaultJooqVersion,
    jooqCodegen := codegenTask.value,
    jooqCodegenConfigFile := None,
    jooqCodegenTargetDirectory := (sourceManaged in Compile).value,
    jooqCodegenConfigRewriteRules := configRewriteRules.value,
    jooqCodegenConfig := codegenConfigTask.value,
    jooqCodegenStrategy := CodegenStrategy.IfAbsent,
    sourceGenerators in Compile += autoCodegenTask.taskValue,
    ivyConfigurations += Jooq,
    autoJooqLibrary := true,
    libraryDependencies ++= {
      if (autoJooqLibrary.value) Seq(
        "org.jooq" % "jooq" % jooqVersion.value, // Add to compile scope
        "org.jooq" % "jooq-codegen" % jooqVersion.value % "jooq"
      )
      else Nil
    },
    libraryDependencies ++= Seq(
      "org.slf4j" % "slf4j-simple" % "1.7.25" % "jooq"
    ),
    javacOptions in Compile ++= {
      if (isJigsawEnabled(sys.props("java.version")))
        Seq("--add-modules", "java.xml.ws.annotation")
      else
        Nil
    }
  ) ++ inConfig(Jooq)(Defaults.configSettings ++ Seq(
    mainClass := Some("org.jooq.util.GenerationTool"),
    javaOptions ++= {
      if (isJigsawEnabled(forkJavaVersion.value))
        Seq("--add-modules", "java.xml.bind")
      else
        Nil
    },
    javaOptions ++= Seq(
      "-classpath", Path.makeString(data(fullClasspath.value)),
      "-Dorg.slf4j.simpleLogger.logFile=System.out",
      "-Dorg.slf4j.simpleLogger.showLogName=false",
      "-Dorg.slf4j.simpleLogger.levelInBrackets=true"
    ),
    forkJavaVersion := {
      javaHome.value.fold(sys.props("java.version")) { home =>
        val releaseFile = home / "release"
        val versionLine = """JAVA_VERSION="(.+)"""".r
        IO.readLines(releaseFile).collectFirst {
          case versionLine(ver) => ver
        }.getOrElse {
          sys.error(s"Cannot detect JAVA_VERSION in $home")
        }
      }
    },
    forkOptions := forkOptionsTask.value
  ))

  private def isJigsawEnabled(javaVersion: String): Boolean =
    javaVersion.takeWhile(_.isDigit).toInt >= 9


  def rewriteRule(name0: String)(f: PartialFunction[xml.Node, Seq[xml.Node]]): RewriteRule = new RewriteRule {
    override val name: String = name0
    override def transform(n: xml.Node): Seq[xml.Node] = f.applyOrElse(n, (_: xml.Node) => n)
  }

  private def configRewriteRules = Def.setting {
    def directory = <directory>{jooqCodegenTargetDirectory.value}</directory>
    Seq(
      rewriteRule("replaceTargetDirectory") {
        case Elem(_, "directory", _, _, _*) => directory
        case e: Elem if e.label == "target" && e.child.forall(_.label != "directory") => e.copy(child = e.child :+ directory)
      }
    )
  }

  private def codegenConfigTask = Def.task {
    val base = baseDirectory.value
    val file = jooqCodegenConfigFile.value.getOrElse(sys.error("required: jooqCodegenConfigFile or jooqCodegenConfig"))
    val transformer = new RuleTransformer(jooqCodegenConfigRewriteRules.value: _*)
    transformer(IO.reader(IO.resolve(base, file))(XML.load))
  }

  private def codegenTask = Def.task {
    val config = jooqCodegenConfig.value
    val main = (mainClass in Jooq).value.getOrElse(sys.error("required: mainClass in jooq"))
    val forkOpts = (forkOptions in Jooq).value
    IO.withTemporaryFile("jooq-codegen-", ".xml") { file =>
      XML.save(file.getAbsolutePath, config, "UTF-8", xmlDecl = true)
      runCodegen(main, file, forkOpts)
    } match {
      case 0 => sourcesIn(packageDir(jooqCodegenTargetDirectory.value, config))
      case e => sys.error(s"jOOQ codegen failure: $e")
    }
  }

  private def autoCodegenTask = Def.taskDyn {
    jooqCodegenStrategy.value match {
      case CodegenStrategy.Always => Def.task(jooqCodegen.value)
      case CodegenStrategy.IfAbsent =>
        Def.taskDyn {
          val fs = sourcesIn(packageDir(jooqCodegenTargetDirectory.value, jooqCodegenConfig.value))
          if (fs.isEmpty)
            Def.task(jooqCodegen.value)
          else
            Def.task(fs)
        }
    }
  }

  private def sourcesIn(dir: File): Seq[File] = (dir ** ("*.java" || "*.scala")).get

  private def packageDir(target: File, config: xml.Node): File = {
    val p = config \ "generator" \ "target" \ "packageName"
    val r = """^\w+(\.\w+)*$""".r
    p.text.trim match {
      case t@r(_) => t.split('.').foldLeft(target)(_ / _)
      case invalid => sys.error(s"invalid packageName format: $invalid")
    }
  }

  private def forkOptionsTask = Def.task {
    ForkOptions(
      javaHome = javaHome.value,
      outputStrategy = outputStrategy.value,
      bootJars = Vector.empty,
      workingDirectory = Some(baseDirectory.value),
      runJVMOptions = javaOptions.value.toVector,
      connectInput = connectInput.value,
      envVars = envVars.value
    )
  }

  private def runCodegen(mainClass: String, config: File, forkOptions: ForkOptions): Int = {
    val process = Fork.java.fork(forkOptions, Seq(mainClass, config.getAbsolutePath))
    try
      process.exitValue()
    catch {
      case _: InterruptedException =>
        process.destroy()
        1
    }
  }

}
