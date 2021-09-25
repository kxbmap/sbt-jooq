package sbtjooq.codegen

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
    final val AutoStrategy = sbtjooq.codegen.AutoStrategy

    type CodegenConfig = sbtjooq.codegen.CodegenConfig
    final val CodegenConfig = sbtjooq.codegen.CodegenConfig

    def addJooqCodegenSettingsTo(config: Configuration): Seq[Setting[_]] =
      jooqCodegenScopedSettings(config)

  }

  override def projectConfigurations: Seq[Configuration] = Seq(JooqCodegen)

  override def projectSettings: Seq[Setting[_]] =
    jooqCodegenDefaultSettings ++ jooqCodegenScopedSettings(Compile)

  override def globalSettings: Seq[Setting[_]] = Seq(
    jooqCodegenConfig := CodegenConfig.empty,
    jooqCodegenVariables := Map.empty,
    jooqCodegenAutoStrategy := AutoStrategy.IfAbsent,
    jooqCodegenGeneratedSources / includeFilter := "*.java" | "*.scala",
  )

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
    javacOptions ++= Codegen.javacOptions(jooqVersion.value, Codegen.compileJavaVersion),
    jooqCodegen := codegenTask.value,
    jooqCodegenIfAbsent := codegenIfAbsentTask.value,
    jooqCodegenIfAbsent / skip := (jooqCodegen / skip).value,
    jooqCodegenVariables ++= Map(
      "TARGET_DIRECTORY" -> sourceManaged.value.toString,
      "RESOURCE_DIRECTORY" -> (JooqCodegen / resourceDirectory).value.toString,
    ),
    jooqCodegenConfigTransformer := Codegen.configTransformer(jooqCodegenVariables.value),
    jooqCodegenTransformedConfigs := transformConfigsTask.value,
    jooqCodegenTransformedConfigFiles := transformedConfigFilesTask(config).value,
    sourceGenerators += sourceGeneratorTask.taskValue,
    jooqCodegenGeneratorTargets := generatorTargetsTask.value,
    jooqCodegenGeneratedSourcesFinders := generatedSourcesFindersTask.value,
    jooqCodegenGeneratedSources := generatedSourcesTask.value,
  ))


  private def transformConfigsTask: Initialize[Task[Seq[Node]]] = Def.task {
    def load(config: CodegenConfig.Single): Node =
      config match {
        case CodegenConfig.File(file) =>
          IO.reader(IO.resolve(baseDirectory.value, file))(XML.load)
        case CodegenConfig.Resource(resource) =>
          ClasspathLoader.using((JooqCodegen / fullClasspath).value) { loader =>
            loader.getResourceAsStream(resource) match {
              case null => throw new MessageOnlyException(s"resource $resource not found in classpath")
              case in => Using.bufferedInputStream(in)(XML.load)
            }
          }
        case CodegenConfig.XML(xml) => xml
      }
    jooqCodegenConfig.value.toSeq.map(load).map(jooqCodegenConfigTransformer.value)
  }

  private def transformedConfigFilesTask(config: Configuration): Initialize[Task[Seq[File]]] = Def.task {
    val configs = jooqCodegenTransformedConfigs.value
    val dir = target.value / "jooq-codegen" / config.name
    IO.createDirectory(dir)
    configs.zipWithIndex.map {
      case (xml, idx) =>
        val file = dir / s"$idx.xml"
        XML.save(file.toString, xml, "UTF-8", xmlDecl = true)
        file
    }
  }

  private def codegenTask: Initialize[Task[Seq[File]]] = Def.task {
    if ((jooqCodegen / skip).value)
      jooqCodegenGeneratedSources.value
    else
      Def.taskDyn(runCodegen(jooqCodegenTransformedConfigFiles.value)).value
  }

  private def codegenIfAbsentTask: Initialize[Task[Seq[File]]] = Def.task {
    if ((jooqCodegenIfAbsent / skip).value) jooqCodegenGeneratedSources.value
    else Def.taskDyn {
      val configs = jooqCodegenGeneratedSourcesFinders.value.collect {
        case (config, finder) if finder.get().isEmpty => config
      }
      runCodegen(configs)
    }.value
  }

  private def runCodegen(configs: Seq[File]): Initialize[Task[Seq[File]]] =
    if (configs.isEmpty) jooqCodegenGeneratedSources
    else Def.sequential(
      (JooqCodegen / run).toTask(configs.mkString(" ", " ", "")),
      jooqCodegenGeneratedSources
    )

  private def sourceGeneratorTask: Initialize[Task[Seq[File]]] = Def.taskDyn {
    jooqCodegenAutoStrategy.value match {
      case AutoStrategy.Always => jooqCodegen
      case AutoStrategy.IfAbsent => jooqCodegenIfAbsent
      case AutoStrategy.Never => jooqCodegenGeneratedSources
    }
  }

  private def generatorTargetsTask: Initialize[Task[Seq[(File, File)]]] = Def.task {
    import sbt.util.CacheImplicits._
    def parse(conf: File): (File, File) = {
      val config = IO.reader(conf)(XML.load)
      val target = file(Codegen.generatorTargetDirectory(config)).getAbsoluteFile
      conf -> Codegen.generatorTargetPackage(config).foldLeft(target)(_ / _)
    }
    val prev = jooqCodegenGeneratorTargets.previous.getOrElse(Seq.empty)
    val files = jooqCodegenTransformedConfigFiles.value
    val store = streams.value.cacheStoreFactory.make("inputs")
    Tracked.diffInputs(store, FileInfo.hash)(files.toSet) { diff =>
      prev.filter(x => diff.unmodified(x._1)) ++ (diff.modified -- diff.removed).map(parse)
    }
  }

  private def generatedSourcesFindersTask: Initialize[Task[Seq[(File, PathFinder)]]] = Def.task {
    jooqCodegenGeneratorTargets.value.map {
      case (conf, target) => conf -> target.descendantsExcept(
        (jooqCodegenGeneratedSources / includeFilter).value,
        (jooqCodegenGeneratedSources / excludeFilter).value)
    }
  }

  private def generatedSourcesTask: Initialize[Task[Seq[File]]] = Def.task {
    jooqCodegenGeneratedSourcesFinders.value.flatMap(_._2.get()).distinct
  }

}
