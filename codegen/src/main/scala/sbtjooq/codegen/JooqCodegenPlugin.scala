package sbtjooq.codegen

import java.nio.file.Files
import sbt._
import sbt.Def.Initialize
import sbt.Keys._
import sbt.io.Using
import sbtjooq.JooqKeys._
import sbtjooq.JooqPlugin
import sbtjooq.codegen.JooqCodegenKeys._
import sbtjooq.codegen.internal.{ClasspathLoader, Codegen, SubstitutionParser}
import scala.xml.{Node, Text, XML}
import scala.xml.transform.{RewriteRule, RuleTransformer}

object JooqCodegenPlugin extends AutoPlugin {

  override def requires: Plugins = JooqPlugin

  object autoImport extends JooqCodegenKeys {

    type AutoStrategy = sbtjooq.codegen.AutoStrategy
    val AutoStrategy = sbtjooq.codegen.AutoStrategy

    type CodegenKey = sbtjooq.codegen.CodegenKey
    val CodegenKey = sbtjooq.codegen.CodegenKey

    def addJooqCodegenSettingsTo(config: Configuration): Seq[Setting[_]] =
      jooqCodegenScopedSettings(config)

  }

  override def projectConfigurations: Seq[Configuration] = Seq(JooqCodegen)

  override def projectSettings: Seq[Setting[_]] =
    jooqCodegenDefaultSettings ++ jooqCodegenScopedSettings(Compile)

  def jooqCodegenDefaultSettings: Seq[Setting[_]] = Seq(
    libraryDependencies ++= Codegen
      .dependencies((JooqCodegen / jooqVersion).value, Codegen.javaVersion((JooqCodegen / javaHome).value))
      .map(_ % JooqCodegen),
    libraryDependencies ++= Classpaths.autoLibraryDependency(
      (JooqCodegen / autoScalaLibrary).value && (JooqCodegen / scalaHome).value.isEmpty && (JooqCodegen / managedScalaInstance).value,
      plugin = false,
      (JooqCodegen / scalaOrganization).value,
      (JooqCodegen / scalaVersion).value)
      .map(_ % JooqCodegen)
  ) ++
    JooqPlugin.jooqScopedSettings(JooqCodegen) ++
    inConfig(JooqCodegen)(Defaults.configSettings ++
      Seq(
        jooqModules := Seq("jooq-codegen"),
      ) ++
      inTask(run)(Seq(
        fork := Codegen.needsFork(jooqVersion.value, Codegen.javaVersion(javaHome.value)),
        mainClass := Some(Codegen.mainClass),
        javaOptions ++= Codegen.javaOptions(jooqVersion.value, Codegen.javaVersion(javaHome.value))
      )))

  def jooqCodegenScopedSettings(config: Configuration): Seq[Setting[_]] = Seq(
    libraryDependencies ++= Codegen
      .compileDependencies((config / jooqVersion).value, Codegen.compileJavaVersion)
      .map(_ % config)
  ) ++ inConfig(config)(Seq(
    jooqCodegen := codegenTask.value,
    jooqCodegen / skip := jooqCodegenConfig.?.value.isEmpty,
    jooqCodegenKeys := Seq(
      baseDirectory,
      config / sourceManaged,
      JooqCodegen / resourceDirectory
    ),
    jooqCodegenConfigVariables := codegenVariablesTask(config).value,
    jooqCodegenConfigVariables ++= sys.env,
    jooqCodegenConfigTransformer := configTransformerTask.value,
    jooqCodegenTransformedConfig := configTransformTask.value,
    jooqCodegenAutoStrategy := AutoStrategy.IfAbsent,
    sourceGenerators += autoCodegenTask.taskValue,
    jooqCodegenGeneratedSources / includeFilter := "*.java" | "*.scala",
    jooqCodegenGeneratedSources := jooqCodegenGeneratedSourcesFinder.value.get,
    jooqCodegenGeneratedSourcesFinder := generatedSourcesFinderTask.value,
    javacOptions ++= Codegen.javacOptions(jooqVersion.value, Codegen.compileJavaVersion)
  ))

  private def codegenVariablesTask(config: Configuration): Initialize[Task[Map[String, String]]] = Def.taskDyn {
    val keys = jooqCodegenKeys.value
    val s = state.value
    val p = thisProjectRef.value
    Def.task {
      CodegenKey.build(keys, config, s, p).value
    }
  }

  private def configTransformerTask: Initialize[Task[RuleTransformer]] = Def.task {
    val vars = jooqCodegenConfigVariables.value
    val parser = new SubstitutionParser(vars)
    new RuleTransformer(new RewriteRule {
      override def transform(n: Node): Seq[Node] = n match {
        case Text(data) =>
          parser.parse(data).fold(
            e => throw new MessageOnlyException(s"Substitution failure: $e"),
            s => Text(s)
          )
        case otherwise => otherwise
      }
    })
  }

  private def configTransformTask: Initialize[Task[Node]] = Def.taskDyn {
    val transformer = jooqCodegenConfigTransformer.value
    val xml = jooqCodegenConfig.?.value match {
      case None => throw new MessageOnlyException("required: jooqCodegenConfig")
      case Some(CodegenConfig.File(file)) =>
        Def.task[Node] {
          IO.reader(IO.resolve(baseDirectory.value, file))(XML.load)
        }
      case Some(CodegenConfig.Classpath(resource)) =>
        Def.task[Node] {
          ClasspathLoader.using((JooqCodegen / fullClasspath).value) { loader =>
            loader.getResourceAsStream(resource) match {
              case null => throw new MessageOnlyException(s"resource $resource not found in classpath")
              case in => Using.bufferedInputStream(in)(XML.load)
            }
          }
        }
      case Some(CodegenConfig.XML(xml)) => Def.task(xml)
    }
    Def.task(transformer(xml.value))
  }

  private def codegenTask: Initialize[Task[Seq[File]]] = Def.taskDyn {
    if ((jooqCodegen / skip).value) Def.task(Seq.empty[File])
    else Def.taskDyn {
      val config = jooqCodegenTransformedConfig.value
      val file = Files.createTempFile("jooq-codegen-", ".xml")
      Def.sequential(
        Def.task(XML.save(file.toString, config, "UTF-8", xmlDecl = true)),
        (JooqCodegen / run).toTask(s" $file"),
        jooqCodegenGeneratedSources
      ).andFinally {
        Files.delete(file)
      }
    }
  }

  private def autoCodegenTask: Initialize[Task[Seq[File]]] = Def.taskDyn {
    if ((jooqCodegen / skip).value) Def.task(Seq.empty[File])
    else Def.taskDyn {
      jooqCodegenAutoStrategy.value match {
        case AutoStrategy.Always => jooqCodegen
        case AutoStrategy.IfAbsent => Def.taskDyn {
          val files = jooqCodegenGeneratedSourcesFinder.value.get
          if (files.isEmpty) jooqCodegen else Def.task(files)
        }
        case AutoStrategy.Never => jooqCodegenGeneratedSources
      }
    }
  }

  private def generatedSourcesFinderTask: Initialize[Task[PathFinder]] = Def.task {
    val config = jooqCodegenTransformedConfig.value
    val target = file(Codegen.generatorTargetDirectory(config)).getAbsoluteFile
    Codegen.generatorTargetPackage(config)
      .foldLeft(target)(_ / _)
      .descendantsExcept(
        (jooqCodegenGeneratedSources / includeFilter).value,
        (jooqCodegenGeneratedSources / excludeFilter).value)
  }

}
