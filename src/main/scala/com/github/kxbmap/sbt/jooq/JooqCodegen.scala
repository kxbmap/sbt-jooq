package com.github.kxbmap.sbt.jooq

import com.github.kxbmap.sbt.jooq.PluginCompat._
import com.github.kxbmap.sbt.jooq.internal.ClasspathLoader
import java.nio.file.Files
import sbt.Attributed.data
import sbt.Keys._
import sbt._
import sbt.plugins.JvmPlugin
import scala.xml.transform.{RewriteRule, RuleTransformer}
import scala.xml.{Elem, XML}

object JooqCodegen extends AutoPlugin {

  val DefaultJooqVersion = "3.10.1"

  override def requires: Plugins = JvmPlugin

  override def projectSettings: Seq[Setting[_]] = jooqCodegenSettings

  object autoImport extends ConfigLocation.Implicits {

    val Jooq = config("jooq").hide

    val jooqVersion = settingKey[String]("jOOQ version")
    val jooqGroupId = settingKey[String]("jOOQ groupId")

    val jooqCodegen = taskKey[Seq[File]]("Run jOOQ codegen")
    val jooqCodegenConfigLocation = settingKey[ConfigLocation]("Location of jOOQ codegen configuration")
    val jooqCodegenTargetDirectory = settingKey[File]("jOOQ codegen target directory")
    val jooqCodegenConfigRewriteRules = settingKey[Seq[RewriteRule]]("jOOQ codegen configuration rewrite rules")
    val jooqCodegenConfig = taskKey[xml.Node]("jOOQ codegen configuration")
    val jooqCodegenStrategy = settingKey[CodegenStrategy]("jOOQ codegen strategy")
    val jooqCodegenGeneratedDirectories = taskKey[Seq[File]]("Generated directories by jOOQ codegen")

    val autoJooqLibrary = settingKey[Boolean]("Add jOOQ dependencies if true")

    val CodegenStrategy = com.github.kxbmap.sbt.jooq.CodegenStrategy

  }

  import CodegenUtil._
  import autoImport.{CodegenStrategy => _, _}

  private val forkJavaVersion = taskKey[String]("fork Java version")

  private lazy val jooqCodegenSettings: Seq[Setting[_]] = Seq(
    jooqVersion := DefaultJooqVersion,
    jooqGroupId := "org.jooq",
    jooqCodegen := codegenTask.value,
    jooqCodegenTargetDirectory := (sourceManaged in Compile).value,
    jooqCodegenConfigRewriteRules := configRewriteRules.value,
    jooqCodegenConfig := codegenConfigTask.value,
    jooqCodegenStrategy := CodegenStrategy.IfAbsent,
    sourceGenerators in Compile += autoCodegenTask.taskValue,
    jooqCodegenGeneratedDirectories := generatedDirectoriesTask.value,
    ivyConfigurations += Jooq,
    autoJooqLibrary := true,
    libraryDependencies ++= {
      if (autoJooqLibrary.value) Seq(
        jooqGroupId.value % "jooq" % jooqVersion.value, // Add to compile scope
        jooqGroupId.value % "jooq-codegen" % jooqVersion.value % "jooq"
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
    fork in run := true,
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
    }
  ))

  private def configRewriteRules = Def.setting {
    def directory = <directory>{jooqCodegenTargetDirectory.value}</directory>
    Seq(
      rewriteRule("replaceTargetDirectory") {
        case Elem(_, "directory", _, _, _*) => directory
        case e: Elem if e.label == "target" && e.child.forall(_.label != "directory") => e.copy(child = e.child :+ directory)
      }
    )
  }

  private def codegenConfigTask = Def.taskDyn {
    val transformer = new RuleTransformer(jooqCodegenConfigRewriteRules.value: _*)
    (jooqCodegenConfigLocation ?? sys.error("required: jooqCodegenConfigLocation or jooqCodegenConfig")).value match {
      case ConfigLocation.File(file) => Def.task {
        transformer(IO.reader(IO.resolve(baseDirectory.value, file))(XML.load))
      }
      case ConfigLocation.Classpath(resource) => Def.task {
        ClasspathLoader.using((fullClasspath in Jooq).value) { loader =>
          val res = if (resource.startsWith("/")) resource.substring(1) else resource
          val xml = loader.getResourceAsStream(res) match {
            case null => sys.error(s"resource $resource not found in classpath")
            case in => Using.bufferedInputStream(in)(XML.load)
          }
          transformer(xml)
        }
      }
    }
  }

  private def codegenTask = Def.taskDyn {
    val config = jooqCodegenConfig.value
    val file = Def.task(Files.createTempFile("jooq-codegen-", ".xml")).value
    Def.sequential(
      Def.task(XML.save(file.toString, config, "UTF-8", xmlDecl = true)),
      (run in Jooq).toTask(s" $file"),
      Def.task(listSourcesIn(jooqCodegenGeneratedDirectories.value))
    ).andFinally {
      Files.delete(file)
    }
  }

  private def autoCodegenTask = Def.taskDyn {
    jooqCodegenStrategy.value match {
      case CodegenStrategy.Always => jooqCodegen
      case CodegenStrategy.IfAbsent => Def.taskDyn {
        val files = listSourcesIn(jooqCodegenGeneratedDirectories.value)
        if (files.isEmpty) jooqCodegen else Def.task(files)
      }
    }
  }

  private def generatedDirectoriesTask = Def.task {
    val target = jooqCodegenTargetDirectory.value
    val config = jooqCodegenConfig.value
    val packageDir = {
      val p = config \ "generator" \ "target" \ "packageName"
      val r = """^\w+(\.\w+)*$""".r
      p.text.trim match {
        case t@r(_) => t.split('.').foldLeft(target)(_ / _)
        case invalid => sys.error(s"invalid packageName format: $invalid")
      }
    }
    Seq(packageDir)
  }

}
