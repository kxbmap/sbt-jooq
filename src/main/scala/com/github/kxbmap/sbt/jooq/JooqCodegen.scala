package com.github.kxbmap.sbt.jooq

import com.github.kxbmap.sbt.jooq.CodegenUtil._
import com.github.kxbmap.sbt.jooq.JooqCodegenKeys._
import com.github.kxbmap.sbt.jooq.internal.PluginCompat._
import com.github.kxbmap.sbt.jooq.internal.{ClasspathLoader, SubstitutionParser}
import java.nio.file.Files
import sbt.Keys._
import sbt._
import sbtslf4jsimple.Slf4jSimplePlugin
import sbtslf4jsimple.Slf4jSimplePlugin.autoImport._
import scala.xml.transform.{RewriteRule, RuleTransformer}
import scala.xml.{Node, Text, XML}

object JooqCodegen extends AutoPlugin {

  val DefaultJooqVersion = "3.11.0"

  override def requires: Plugins = Slf4jSimplePlugin

  object autoImport extends JooqCodegenKeys with CodegenConfig.Implicits {

    type CodegenStrategy = com.github.kxbmap.sbt.jooq.CodegenStrategy
    val CodegenStrategy = com.github.kxbmap.sbt.jooq.CodegenStrategy

    def addJooqCodegenSettingsTo(config: Configuration): Seq[Setting[_]] =
      JooqCodegen.jooqCodegenScopedSettings(config)

  }

  override def projectSettings: Seq[Setting[_]] =
    jooqCodegenDefaultSettings ++ jooqCodegenScopedSettings(Compile)

  def jooqCodegenDefaultSettings: Seq[Setting[_]] = Seq(
    jooqVersion := DefaultJooqVersion,
    jooqOrganization := "org.jooq",
    autoJooqLibrary := true,
    ivyConfigurations += Jooq,
    libraryDependencies ++= {
      if (autoJooqLibrary.value)
        Seq(jooqOrganization.value % "jooq-codegen" % jooqVersion.value % "jooq")
      else
        Nil
    }
  ) ++ inConfig(Jooq)(Defaults.configSettings ++ inTask(run)(Seq(
    fork := true,
    mainClass := Some(CrossVersion.partialVersion(jooqVersion.value) match {
      case Some((x, y)) if x < 3 || x == 3 && y < 11 => "org.jooq.util.GenerationTool"
      case _ => "org.jooq.codegen.GenerationTool"
    }),
    javaOptions ++= {
      if (isJigsawEnabled(javaHome.value.fold(sys.props("java.version"))(parseJavaVersion)))
        Seq("--add-modules", "java.xml.bind")
      else
        Nil
    }
  ))) ++ slf4jSimpleScopedSettings(Jooq) ++ inConfig(Jooq)(Seq(
    slf4jSimplePropertiesType := Slf4jSimplePropertiesType.JavaOptions,
    slf4jSimpleLogFile := "System.out",
    slf4jSimpleCacheOutputStream := true,
    slf4jSimpleShowThreadName := false,
    slf4jSimpleShowLogName := false,
    slf4jSimpleLevelInBrackets := true
  ))

  def jooqCodegenScopedSettings(config: Configuration): Seq[Setting[_]] = Seq(
    libraryDependencies ++= {
      if (autoJooqLibrary.value)
        Seq(jooqOrganization.value % "jooq" % jooqVersion.value % config)
      else
        Nil
    }
  ) ++ inConfig(config)(Seq(
    jooqCodegen := codegenTask.value,
    skip in jooqCodegen := jooqCodegenConfig.?.value.isEmpty,
    jooqCodegenConfigSubstitutions := configSubstitutions.value,
    jooqCodegenConfigTransformer := configTransformer.value,
    jooqCodegenTransformedConfig := configTransformTask.value,
    jooqCodegenStrategy := CodegenStrategy.IfAbsent,
    sourceGenerators += autoCodegenTask.taskValue,
    includeFilter in jooqCodegenGeneratedSources := "*.java" | "*.scala",
    jooqCodegenGeneratedSources := jooqCodegenGeneratedSourcesFinder.value.get,
    jooqCodegenGeneratedSourcesFinder := generatedSourcesFinderTask.value,
    javacOptions ++= {
      if (isJigsawEnabled(sys.props("java.version")))
        Seq("--add-modules", "java.xml.ws.annotation")
      else
        Nil
    }
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

  private def configTransformTask = Def.taskDyn(Def.taskDyn { // To avoid evaluate ?? on project loading
    val transformer = jooqCodegenConfigTransformer.value
    val xml = (jooqCodegenConfig ?? sys.error("required: jooqCodegenConfig")).value match {
      case CodegenConfig.File(file) => Def.task[Node] {
        IO.reader(IO.resolve(baseDirectory.value, file))(XML.load)
      }
      case CodegenConfig.Resource(resource) => Def.task[Node] {
        ClasspathLoader.using((fullClasspath in Jooq).value) { loader =>
          val res = if (resource.startsWith("/")) resource.substring(1) else resource
          loader.getResourceAsStream(res) match {
            case null => sys.error(s"resource $resource not found in classpath")
            case in => Using.bufferedInputStream(in)(XML.load)
          }
        }
      }
      case CodegenConfig.XML(xml) => Def.task(xml)
    }
    xml.map(transformer)
  })

  private def codegenTask = Def.taskDyn {
    if ((skip in jooqCodegen).value) Def.task(Seq.empty[File])
    else Def.taskDyn {
      val config = jooqCodegenTransformedConfig.value
      val file = Files.createTempFile("jooq-codegen-", ".xml")
      Def.sequential(
        Def.task(XML.save(file.toString, config, "UTF-8", xmlDecl = true)),
        (run in Jooq).toTask(s" $file"),
        Def.task(jooqCodegenGeneratedSourcesFinder.value.get)
      ).andFinally {
        Files.delete(file)
      }
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
        case CodegenStrategy.Never => Def.task(Seq.empty[File])
      }
    }
  }

  private def generatedSourcesFinderTask = Def.task {
    val config = jooqCodegenTransformedConfig.value
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
    packageDir.descendantsExcept(
      (includeFilter in jooqCodegenGeneratedSources).value,
      (excludeFilter in jooqCodegenGeneratedSources).value)
  }

}
