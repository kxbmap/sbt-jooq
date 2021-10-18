package sbtjooq.codegen

import sbt._
import sbt.Def.Initialize
import sbt.Keys._
import sbt.io.Using
import sbtjooq.JooqKeys._
import sbtjooq.JooqPlugin
import sbtjooq.codegen.JooqCodegenKeys._
import sbtjooq.codegen.internal._
import scala.xml.{Node, XML}

object JooqCodegenPlugin extends AutoPlugin {

  override def requires: Plugins = JooqPlugin

  object autoImport extends JooqCodegenKeys {

    type CodegenMode = sbtjooq.codegen.CodegenMode
    final val CodegenMode = sbtjooq.codegen.CodegenMode

    type CodegenConfig = sbtjooq.codegen.CodegenConfig
    final val CodegenConfig = sbtjooq.codegen.CodegenConfig

  }

  override def projectConfigurations: Seq[Configuration] = Seq(JooqCodegen)

  override def projectSettings: Seq[Setting[_]] =
    jooqCodegenDefaultSettings ++
      inConfig(Compile)(jooqCodegenSettings) ++
      jooqCodegenDependencies(Compile)

  override def globalSettings: Seq[Setting[_]] = Seq(
    jooqCodegenMode := CodegenMode.Auto,
    jooqCodegenConfig := CodegenConfig.empty,
    jooqCodegenVariables := Map.empty,
    jooqCodegenVariableExpander := VariableExpander.default,
    jooqCodegenGeneratedSources / includeFilter := "*.java" | "*.scala",
  )

  private def jooqCodegenDefaultSettings: Seq[Setting[_]] =
    inConfig(JooqCodegen)(
      Defaults.configSettings ++
        Seq(
          jooqModules := Seq("jooq-codegen")
        ) ++
        inTask(run)(
          Seq(
            mainClass := Some(Codegen.mainClass),
            fork := Codegen.needsFork(autoJooqLibrary.value, jooqVersion.value, javaHome.value),
            javaOptions ++= Codegen.javaOptions(autoJooqLibrary.value, jooqVersion.value, javaHome.value),
          )
        )
    ) ++
      JooqPlugin.jooqDependencies(JooqCodegen) ++
      Seq(
        libraryDependencies ++=
          Codegen.dependencies(
            (JooqCodegen / autoJooqLibrary).value,
            (JooqCodegen / jooqVersion).value,
            (JooqCodegen / run / javaHome).value,
          ).map(_ % JooqCodegen),
        libraryDependencies ++=
          Classpaths.autoLibraryDependency(
            (JooqCodegen / autoScalaLibrary).value
              && (JooqCodegen / scalaHome).value.isEmpty
              && (JooqCodegen / managedScalaInstance).value,
            plugin = false,
            (JooqCodegen / scalaOrganization).value,
            (JooqCodegen / scalaVersion).value,
          ).map(_ % JooqCodegen),
      )

  def jooqCodegenDependencies(config: Configuration): Seq[Setting[_]] = Seq(
    libraryDependencies ++=
      Codegen.compileDependencies(
        (config / autoJooqLibrary).value,
        (config / compile / javaHome).value,
        (JooqCodegen / jooqVersion).value,
        (JooqCodegen / run / javaHome).value,
      ).map(_ % config)
  )

  lazy val jooqCodegenSettings: Seq[Setting[_]] = Seq(
    javacOptions ++=
      Codegen.javacOptions(
        autoJooqLibrary.value,
        (compile / javaHome).value,
        (JooqCodegen / jooqVersion).value,
        (JooqCodegen / run / javaHome).value,
      ),
    jooqCodegen := codegenTask.value,
    jooqCodegen / skip := skip.value,
    jooqCodegenIfAbsent := codegenIfAbsentTask.value,
    jooqCodegenIfAbsent / skip := (jooqCodegen / skip).value,
    sourceGenerators ++= sourceGeneratorsSetting.value,
    jooqSource := {
      if (jooqCodegenMode.value.isUnmanaged)
        sourceDirectory.value / "jooq-generated"
      else
        sourceManaged.value
    },
    unmanagedSourceDirectories := {
      if (jooqCodegenMode.value.isUnmanaged)
        (unmanagedSourceDirectories.value :+ jooqSource.value).distinct
      else
        unmanagedSourceDirectories.value
    },
    managedSourceDirectories := {
      if (jooqCodegenMode.value.isUnmanaged)
        managedSourceDirectories.value
      else
        (managedSourceDirectories.value :+ jooqSource.value).distinct
    },
    jooqCodegenVariables ++= Map(
      "RESOURCE_DIRECTORY" -> (JooqCodegen / resourceDirectory).value
    ),
    jooqCodegenConfigTransformer := ConfigTransformer(
      jooqSource.value,
      jooqCodegenVariables.value,
      VariableExpander(jooqCodegenVariableExpander.value),
    ),
    jooqCodegenTransformedConfigs := transformConfigsTask.value,
    jooqCodegenTransformedConfigFiles := transformedConfigFilesTask.value,
    jooqCodegenGeneratorTargets := generatorTargetsTask.value,
    jooqCodegenGeneratedSourcesFinders := generatedSourcesFindersTask.value,
    jooqCodegenGeneratedSources := generatedSourcesTask.value,
  )

  private def codegenTask: Initialize[Task[Seq[File]]] = Def.task {
    if ((jooqCodegen / skip).value)
      jooqCodegenGeneratedSources.value
    else
      Def.taskDyn(
        Def.sequential(
          warnIfConfigIsEmpty,
          runCodegen(jooqCodegenTransformedConfigFiles.value),
        )
      ).value
  }

  private def codegenIfAbsentTask: Initialize[Task[Seq[File]]] = Def.task {
    if ((jooqCodegenIfAbsent / skip).value)
      jooqCodegenGeneratedSources.value
    else
      Def.taskDyn(
        Def.sequential(
          warnIfConfigIsEmpty,
          runCodegen(jooqCodegenGeneratedSourcesFinders.value.collect {
            case (config, finder) if finder.get().isEmpty => config
          }),
        )
      ).value
  }

  private def runCodegen(configs: Seq[File]): Initialize[Task[Seq[File]]] =
    if (configs.isEmpty) jooqCodegenGeneratedSources
    else
      Def.sequential(
        (JooqCodegen / run).toTask(configs.mkString(" ", " ", "")),
        jooqCodegenGeneratedSources,
      )

  private lazy val warnIfConfigIsEmpty: Initialize[Task[Unit]] = Def.task {
    if (jooqCodegenConfig.value.isEmpty) {
      val conf = configuration.value.id
      streams.value.log.warn(
        s"""Skip jOOQ code generation due to `$conf / jooqCodegenConfig` is empty.
           |To turn off this warning,
           |- `set $conf / jooqCodegenConfig := <your configuration>` or
           |- `set $conf / jooqCodegen / skip := true`
           |""".stripMargin
      )
    }
  }

  private def sourceGeneratorsSetting: Initialize[Seq[Task[Seq[File]]]] = Def.setting {
    jooqCodegenMode.value match {
      case CodegenMode.Auto => jooqCodegenIfAbsent.taskValue :: Nil
      case CodegenMode.Always => jooqCodegen.taskValue :: Nil
      case CodegenMode.Unmanaged => Nil
    }
  }

  private def transformConfigsTask: Initialize[Task[Seq[Node]]] = Def.taskDyn {
    def load(config: CodegenConfig.Single): Initialize[Task[Node]] =
      config match {
        case CodegenConfig.FromFile(file) => Def.task(IO.reader(file.getAbsoluteFile)(XML.load))
        case CodegenConfig.FromXML(xml) => Def.task(xml)
        case CodegenConfig.FromResource(resource) =>
          Def.task(ClasspathLoader.using((JooqCodegen / fullClasspath).value) { loader =>
            loader.getResourceAsStream(resource) match {
              case null => throw new MessageOnlyException(s"resource $resource not found in classpath")
              case in => Using.bufferedInputStream(in)(XML.load)
            }
          })
      }
    val config = jooqCodegenConfig.value
    val transform = jooqCodegenConfigTransformer.value
    config.toSeq.map(load(_).map(transform)).joinWith(_.join)
  }

  private def transformedConfigFilesTask: Initialize[Task[Seq[File]]] = Def.task {
    val configs = jooqCodegenTransformedConfigs.value
    val dir = (jooqCodegen / streams).value.cacheDirectory
    configs.zipWithIndex.map {
      case (xml, idx) =>
        val file = dir / s"$idx.xml"
        XML.save(file.toString, xml, "UTF-8", xmlDecl = true)
        file
    }
  }

  private def generatorTargetsTask: Initialize[Task[Seq[(File, File)]]] = Def.task {
    import sbt.util.CacheImplicits._
    def parse(file: File): (File, File) = file -> GeneratorTarget.get(IO.reader(file)(XML.load))
    val prev = jooqCodegenGeneratorTargets.previous.getOrElse(Seq.empty)
    val files = jooqCodegenTransformedConfigFiles.value
    val store = (jooqCodegen / streams).value.cacheStoreFactory.make("files")
    Tracked.diffInputs(store, FileInfo.hash)(files.toSet) { diff =>
      prev.filter(x => diff.unmodified(x._1)) ++ (diff.modified -- diff.removed).map(parse)
    }
  }

  private def generatedSourcesFindersTask: Initialize[Task[Seq[(File, PathFinder)]]] = Def.task {
    jooqCodegenGeneratorTargets.value.map {
      case (conf, target) =>
        conf -> target.descendantsExcept(
          (jooqCodegenGeneratedSources / includeFilter).value,
          (jooqCodegenGeneratedSources / excludeFilter).value,
        )
    }
  }

  private def generatedSourcesTask: Initialize[Task[Seq[File]]] = Def.task {
    jooqCodegenGeneratedSourcesFinders.value.flatMap(_._2.get()).distinct
  }

}
