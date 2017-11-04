package com.github.kxbmap.sbt.jooq

import com.github.kxbmap.sbt.jooq.PluginCompat._
import com.github.kxbmap.sbt.jooq.internal.{ClasspathLoader, SubstitutionParser}
import java.nio.file.Files
import sbt.Attributed.data
import sbt.Keys._
import sbt._
import sbt.plugins.JvmPlugin
import scala.xml.transform.{RewriteRule, RuleTransformer}
import scala.xml.{Node, Text, XML}

object JooqCodegen extends AutoPlugin {

  val DefaultJooqVersion = "3.10.1"

  override def requires: Plugins = JvmPlugin

  object autoImport extends ConfigLocation.Implicits {

    val Jooq = config("jooq").hide

    val jooqVersion = settingKey[String]("jOOQ version")
    val jooqGroupId = settingKey[String]("jOOQ groupId")
    val autoJooqLibrary = settingKey[Boolean]("Add jOOQ dependencies if true")

    val jooqCodegen = taskKey[Seq[File]]("Run jOOQ codegen")
    val jooqCodegenConfigLocation = settingKey[ConfigLocation]("Location of jOOQ codegen configuration")
    val jooqCodegenConfigSubstitutions = settingKey[Map[String, String]]("jOOQ codegen configuration text substitutions")
    val jooqCodegenConfigTransformer = settingKey[Node => Node]("jOOQ codegen configuration transform function")
    val jooqCodegenConfig = taskKey[Node]("jOOQ codegen configuration")
    val jooqCodegenStrategy = settingKey[CodegenStrategy]("jOOQ codegen strategy")
    val jooqCodegenGeneratedSourcesFinder = taskKey[PathFinder]("PathFinder for jOOQ codegen generated sources")

    val CodegenStrategy = com.github.kxbmap.sbt.jooq.CodegenStrategy

    def jooqCodegenSettingsIn(config: Configuration): Seq[Setting[_]] =
      JooqCodegen.jooqCodegenSettingsIn(config)

  }

  import CodegenUtil._
  import autoImport.{CodegenStrategy => _, _}

  override lazy val projectSettings: Seq[Setting[_]] =
    jooqCodegenCoreSettings ++ jooqCodegenSettingsIn(Compile) ++ jooqCodegenRunnerSettings

  private def jooqCodegenCoreSettings: Seq[Setting[_]] = Seq(
    jooqVersion := DefaultJooqVersion,
    jooqGroupId := "org.jooq",
    autoJooqLibrary := true
  )

  private def jooqCodegenSettingsIn(config: Configuration): Seq[Setting[_]] = Seq(
    libraryDependencies ++= {
      if (autoJooqLibrary.value)
        Seq(jooqGroupId.value % "jooq" % jooqVersion.value % config)
      else
        Nil
    }
  ) ++ inConfig(config)(Seq(
    jooqCodegen := codegenTask.value,
    skip in jooqCodegen := false,
    jooqCodegenConfigSubstitutions := configSubstitutions.value,
    jooqCodegenConfigTransformer := configTransformer.value,
    jooqCodegenConfig := codegenConfigTask.value,
    jooqCodegenStrategy := CodegenStrategy.IfAbsent,
    sourceGenerators += autoCodegenTask.taskValue,
    jooqCodegenGeneratedSourcesFinder := generatedSourcesFinderTask.value,
    javacOptions ++= {
      if (isJigsawEnabled(sys.props("java.version")))
        Seq("--add-modules", "java.xml.ws.annotation")
      else
        Nil
    }
  ))

  private def jooqCodegenRunnerSettings: Seq[Setting[_]] = Seq(
    ivyConfigurations += Jooq,
    libraryDependencies ++= {
      if (autoJooqLibrary.value)
        Seq(jooqGroupId.value % "jooq-codegen" % jooqVersion.value % "jooq")
      else
        Nil
    },
    libraryDependencies ++= Seq(
      "org.slf4j" % "slf4j-simple" % "1.7.25" % "jooq"
    )
  ) ++ inConfig(Jooq)(Defaults.configSettings ++ Seq(
    mainClass := Some("org.jooq.util.GenerationTool"),
    fork in run := true,
    javaOptions ++= {
      if (isJigsawEnabled(javaHome.value.fold(sys.props("java.version"))(parseJavaVersion)))
        Seq("--add-modules", "java.xml.bind")
      else
        Nil
    },
    javaOptions ++= Seq(
      "-classpath", Path.makeString(data(fullClasspath.value)),
      "-Dorg.slf4j.simpleLogger.logFile=System.out",
      "-Dorg.slf4j.simpleLogger.showLogName=false",
      "-Dorg.slf4j.simpleLogger.levelInBrackets=true"
    )
  ))


  private def configSubstitutions = Def.setting {
    def kv[T](k: SettingKey[T], v: T) = (k.key.label, v.toString)
    val kvs = Map(
      kv(baseDirectory, baseDirectory.value),
      kv(sourceDirectory, sourceDirectory.value),
      kv(sourceManaged, sourceManaged.value)
    )
    sys.env ++ kvs
  }

  private def configTransformer = Def.setting {
    val substitutions = jooqCodegenConfigSubstitutions.value
    val parser = new SubstitutionParser(substitutions)
    new RuleTransformer(new RewriteRule {
      override def transform(n: Node): Seq[Node] = n match {
        case Text(data) =>
          parser.parse(data).fold(
            e => sys.error(s"Substitution failure: $e"),
            s => Text(s)
          )
        case otherwise => otherwise
      }
    })
  }

  private def codegenConfigTask = Def.taskDyn(Def.taskDyn { // To avoid evaluate ?? on project loading
    val transformer = jooqCodegenConfigTransformer.value
    val xml =
      (jooqCodegenConfigLocation ?? sys.error("required: jooqCodegenConfigLocation or jooqCodegenConfig")).value match {
        case ConfigLocation.File(file) => Def.task {
          IO.reader(IO.resolve(baseDirectory.value, file))(XML.load)
        }
        case ConfigLocation.Classpath(resource) => Def.task {
          ClasspathLoader.using((fullClasspath in Jooq).value) { loader =>
            val res = if (resource.startsWith("/")) resource.substring(1) else resource
            loader.getResourceAsStream(res) match {
              case null => sys.error(s"resource $resource not found in classpath")
              case in => Using.bufferedInputStream(in)(XML.load)
            }
          }
        }
      }
    xml.map(transformer)
  })

  private def codegenTask = Def.taskDyn {
    val config = jooqCodegenConfig.value
    val file = Def.task(Files.createTempFile("jooq-codegen-", ".xml")).value
    Def.sequential(
      Def.task(XML.save(file.toString, config, "UTF-8", xmlDecl = true)),
      (run in Jooq).toTask(s" $file"),
      Def.task(jooqCodegenGeneratedSourcesFinder.value.get)
    ).andFinally {
      Files.delete(file)
    }
  }

  private def autoCodegenTask = Def.taskDyn {
    if ((skip in jooqCodegen).value) Def.task(Seq.empty[File])
    else Def.taskDyn {
      jooqCodegenStrategy.value match {
        case CodegenStrategy.Always => jooqCodegen
        case CodegenStrategy.IfAbsent => Def.taskDyn {
          val files = jooqCodegenGeneratedSourcesFinder.value.get
          if (files.isEmpty) jooqCodegen else Def.task(files)
        }
      }
    }
  }

  private def generatedSourcesFinderTask = Def.task {
    val config = jooqCodegenConfig.value
    val target = config \ "generator" \ "target"
    val targetDir = file((target \ "directory").text.trim)
    val packageDir = {
      val p = target \ "packageName"
      val r = """^\w+(\.\w+)*$""".r
      p.text.trim match {
        case t@r(_) => t.split('.').foldLeft(targetDir)(_ / _)
        case invalid => sys.error(s"invalid packageName format: $invalid")
      }
    }
    packageDir ** ("*.java" || "*.scala")
  }

}
