package sbtjooq.codegen

import java.nio.file.Files
import sbt._
import sbt.Def.Initialize
import sbt.Keys._
import sbt.io.Using
import sbtjooq.JooqKeys._
import sbtjooq.JooqPlugin
import sbtjooq.codegen.JooqCodegenKeys._
import sbtjooq.codegen.internal.{ClasspathLoader, Codegen}
import scala.xml.{Node, XML}

object JooqCodegenPlugin extends AutoPlugin {

  override def requires: Plugins = JooqPlugin

  object autoImport extends JooqCodegenKeys {

    type AutoStrategy = sbtjooq.codegen.AutoStrategy
    val AutoStrategy = sbtjooq.codegen.AutoStrategy

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
    jooqCodegenVariables := Map(
      "TARGET_DIRECTORY" -> sourceManaged.value.toString,
      "RESOURCE_DIRECTORY" -> (JooqCodegen / resourceDirectory).value.toString,
    ),
    jooqCodegenConfigTransformer := Codegen.configTransformer(jooqCodegenVariables.value),
    jooqCodegenTransformedConfig := configTransformTask.value,
    jooqCodegenAutoStrategy := AutoStrategy.IfAbsent,
    sourceGenerators += autoCodegenTask.taskValue,
    jooqCodegenGeneratedSources / includeFilter := "*.java" | "*.scala",
    jooqCodegenGeneratedSources := jooqCodegenGeneratedSourcesFinder.value.get,
    jooqCodegenGeneratedSourcesFinder := generatedSourcesFinderTask.value,
    javacOptions ++= Codegen.javacOptions(jooqVersion.value, Codegen.compileJavaVersion)
  ))


  private def configTransformTask: Initialize[Task[Node]] = Def.taskDyn {
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
    Def.task(jooqCodegenConfigTransformer.value(xml.value))
  }

  private def codegenTask: Initialize[Task[Seq[File]]] = Def.task {
    if ((jooqCodegen / skip).value) Seq.empty[File]
    else Def.taskDyn {
      val file = Files.createTempFile("jooq-codegen-", ".xml")
      Def.sequential(
        Def.task(XML.save(file.toString, jooqCodegenTransformedConfig.value, "UTF-8", xmlDecl = true)),
        (JooqCodegen / run).toTask(s" $file"),
        jooqCodegenGeneratedSources
      ).andFinally {
        Files.delete(file)
      }
    }.value
  }

  private def autoCodegenTask: Initialize[Task[Seq[File]]] = Def.task {
    if ((jooqCodegen / skip).value)
      Def.task {
        if (jooqCodegenConfig.?.value.isEmpty) Seq.empty[File]
        else jooqCodegenGeneratedSources.value
      }.value
    else
      Def.taskDyn {
        jooqCodegenAutoStrategy.value match {
          case AutoStrategy.Always => jooqCodegen
          case AutoStrategy.IfAbsent => Def.task {
            if (jooqCodegenGeneratedSourcesFinder.value.get.isEmpty)
              jooqCodegen.value
            else
              jooqCodegenGeneratedSources.value
          }
          case AutoStrategy.Never => jooqCodegenGeneratedSources
        }
      }.value
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
