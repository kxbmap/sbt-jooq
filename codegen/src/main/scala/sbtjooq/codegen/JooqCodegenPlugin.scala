package sbtjooq.codegen

import java.nio.file.Files
import sbt.Keys._
import sbt._
import sbt.io.Using
import sbtjooq.JooqKeys._
import sbtjooq.JooqPlugin
import sbtjooq.codegen.JooqCodegenKeys._
import sbtjooq.codegen.internal.{ClasspathLoader, Codegen, SubstitutionParser}
import scala.xml.transform.{RewriteRule, RuleTransformer}
import scala.xml.{Node, Text, XML}

object JooqCodegenPlugin extends AutoPlugin {

  override def requires: Plugins = JooqPlugin

  object autoImport extends JooqCodegenKeys {

    type CodegenStrategy = sbtjooq.codegen.CodegenStrategy
    val CodegenStrategy = sbtjooq.codegen.CodegenStrategy

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
        jooqModules := Seq("jooq-codegen")
      ) ++
      inTask(run)(Seq(
        fork := Codegen.needsFork(jooqVersion.value, Codegen.javaVersion(javaHome.value)),
        mainClass := Some(Codegen.mainClass(jooqVersion.value)),
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
    jooqCodegenStrategy := CodegenStrategy.IfAbsent,
    sourceGenerators += autoCodegenTask.taskValue,
    jooqCodegenGeneratedSources / includeFilter := "*.java" | "*.scala",
    jooqCodegenGeneratedSources := jooqCodegenGeneratedSourcesFinder.value.get,
    jooqCodegenGeneratedSourcesFinder := generatedSourcesFinderTask.value,
    javacOptions ++= Codegen.javacOptions(jooqVersion.value, Codegen.compileJavaVersion)
  ))

  private def codegenVariablesTask(config: Configuration) = Def.taskDyn {
    val keys = jooqCodegenKeys.value
    val s = state.value
    val p = thisProjectRef.value
    Def.task {
      CodegenKey.build(keys, config, s, p).value
    }
  }

  private def configTransformerTask =
    jooqCodegenConfigVariables.map { vars =>
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

  private def configTransformTask = Def.taskDyn {
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
    xml.map(transformer)
  }

  private def codegenTask = Def.taskDyn {
    if ((jooqCodegen / skip).value) Def.task(Seq.empty[File])
    else Def.taskDyn {
      val config = jooqCodegenTransformedConfig.value
      val file = Files.createTempFile("jooq-codegen-", ".xml")
      Def.sequential(
        Def.task(XML.save(file.toString, config, "UTF-8", xmlDecl = true)),
        (JooqCodegen / run).toTask(s" $file"),
        Def.task(jooqCodegenGeneratedSourcesFinder.value.get)
      ).andFinally {
        Files.delete(file)
      }
    }
  }

  private def autoCodegenTask = Def.taskDyn {
    if ((jooqCodegen / skip).value) Def.task(Seq.empty[File])
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
    val targetDir = {
      val dir = (target \ "directory").text.trim match {
        case "" => "target/generated-sources/jooq"
        case text => text
      }
      file(dir).getAbsoluteFile
    }
    val packageDir = {
      val regex = """^\w+(\.\w+)*$""".r
      val pkg = (target \ "packageName").text.trim match {
        case "" => "org.jooq.generated"
        case txt@regex(_) => txt
        case invalid => throw new MessageOnlyException(s"invalid packageName format: $invalid")
      }
      pkg.split('.').foldLeft(targetDir)(_ / _)
    }
    packageDir.descendantsExcept(
      (jooqCodegenGeneratedSources / includeFilter).value,
      (jooqCodegenGeneratedSources / excludeFilter).value)
  }

}
