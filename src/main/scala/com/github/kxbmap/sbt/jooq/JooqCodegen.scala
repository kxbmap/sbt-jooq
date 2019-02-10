package com.github.kxbmap.sbt.jooq

import com.github.kxbmap.sbt.jooq.CodegenUtil._
import com.github.kxbmap.sbt.jooq.JooqCodegenKeys._
import com.github.kxbmap.sbt.jooq.internal.{ClasspathLoader, SubstitutionParser}
import java.nio.file.Files
import sbt.Keys._
import sbt._
import sbt.io.Using
import sbtslf4jsimple.Slf4jSimpleKeys._
import sbtslf4jsimple.Slf4jSimplePlugin
import scala.xml.transform.{RewriteRule, RuleTransformer}
import scala.xml.{Node, Text, XML}

object JooqCodegen extends AutoPlugin {

  val DefaultJooqVersion = "3.11.9"

  override def requires: Plugins = Slf4jSimplePlugin

  object autoImport extends JooqCodegenKeys with CodegenConfig.Implicits {

    type CodegenStrategy = com.github.kxbmap.sbt.jooq.CodegenStrategy
    val CodegenStrategy = com.github.kxbmap.sbt.jooq.CodegenStrategy

    type CodegenKey = com.github.kxbmap.sbt.jooq.CodegenKey
    val CodegenKey = com.github.kxbmap.sbt.jooq.CodegenKey

    def addJooqCodegenSettingsTo(config: Configuration): Seq[Setting[_]] =
      JooqCodegen.jooqCodegenScopedSettings(config)

  }

  override def projectConfigurations: Seq[Configuration] = Seq(Jooq)

  override def projectSettings: Seq[Setting[_]] =
    jooqCodegenDefaultSettings ++ jooqCodegenScopedSettings(Compile)

  def jooqCodegenDefaultSettings: Seq[Setting[_]] = Seq(
    jooqVersion := DefaultJooqVersion,
    jooqOrganization := "org.jooq",
    autoJooqLibrary := true,
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
  ))) ++ Slf4jSimplePlugin.slf4jSimpleScopedSettings(Jooq) ++ inConfig(Jooq)(Seq(
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
    jooqCodegenKeys := sys.env,
    jooqCodegenKeys ++= Seq(baseDirectory, sourceManaged in config),
    jooqCodegenSubstitutions := substitutionsTask(config).value,
    jooqCodegenConfigTransformer := configTransformerTask.value,
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


  private def substitutionsTask(config: Configuration) = Def.taskDyn {
    val configKeys = jooqCodegenKeys.value
    val extracted = Project.extract(state.value)
    val project = thisProjectRef.value
    def scope(scoped: Scoped): Scope = {
      val scope0 = scoped.scope
      if (scope0.project == This) scope0 in project else scope0
    }
    def entry(key: CodegenKey): Seq[Task[(String, Any)]] = key match {
      case CodegenKey.Setting(s) => keys(s, config).map(k => task(k -> (extracted.get(s in scope(s)): Any)))
      case CodegenKey.Task(t) => keys(t, config).map(k => t.map(v => k -> (v: Any)))
      case CodegenKey.Constant(k, v) => Seq(task(k -> v))
      case CodegenKey.Mapped(e, f) => entry(e).map(_.map { case (k, v) => k -> (f(v): Any) })
      case CodegenKey.Named(e, k) => entry(e).map(_.map { case (_, v) => k -> v })
    }
    Def.task[Seq[(String, String)]] {
      configKeys.distinct.flatMap(entry).map(_.map {
        case (k, v) => k -> v.toString
      }).join.value
    }
  }

  private def keys(scoped: Scoped, config: Configuration): Seq[String] = keys(scoped.scope, scoped.key, config)

  private def keys(scoped: ScopedKey[_], config: Configuration): Seq[String] = keys(scoped.scope, scoped.key, config)

  private def keys(task: Task[_], config: Configuration): Seq[String] =
    task.info.name.map(Seq(_))
      .orElse(task.info.attributes.get(taskDefinitionKey).map(keys(_, config)))
      .getOrElse(sys.error("anonymous task"))

  private def keys(scope: Scope, attrKey: AttributeKey[_], config: Configuration): Seq[String] = {
    val prj = scope.project.toOption.collect {
      case LocalProject(id) => id + "/"
      case LocalRootProject => "LocalRootProject/"
    }
    val conf = scope.config.toOption.fold(Seq(none[String])) { c =>
      Some(c.name + ":") :: (if (c.name == config.name) None :: Nil else Nil)
    }
    val task = scope.task.toOption.map(_.label + "::")

    conf.map(Seq(prj, _, task).flatten.mkString + attrKey.label)
  }


  private def configTransformerTask =
    jooqCodegenSubstitutions.map { substitutions =>
      val parser = new SubstitutionParser(substitutions.toMap)
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

  private def configTransformTask = Def.taskDyn {
    val transformer = jooqCodegenConfigTransformer.value
    val xml = jooqCodegenConfig.?.value.fold(sys.error("required: jooqCodegenConfig")) {
      case CodegenConfig.File(file) => Def.task[Node] {
        IO.reader(IO.resolve(baseDirectory.value, file))(XML.load)
      }
      case CodegenConfig.Classpath(resource) => Def.task[Node] {
        ClasspathLoader.using((fullClasspath in Jooq).value) { loader =>
          loader.getResourceAsStream(resource) match {
            case null => sys.error(s"resource $resource not found in classpath")
            case in => Using.bufferedInputStream(in)(XML.load)
          }
        }
      }
      case CodegenConfig.XML(xml) => Def.task(xml)
    }
    xml.map(transformer)
  }

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
    val targetDir = file((target \ "directory").text.trim).getAbsoluteFile
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
