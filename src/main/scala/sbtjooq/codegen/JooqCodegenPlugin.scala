package sbtjooq.codegen

import java.nio.file.Files
import sbt.Keys._
import sbt._
import sbt.io.Using
import sbtjooq.JooqKeys._
import sbtjooq.JooqPlugin
import sbtjooq.codegen.JooqCodegenKeys._
import sbtjooq.internal.JavaUtil._
import sbtjooq.internal.{ClasspathLoader, SubstitutionParser}
import sbtslf4jsimple.Slf4jSimpleKeys._
import sbtslf4jsimple.Slf4jSimplePlugin
import scala.xml.transform.{RewriteRule, RuleTransformer}
import scala.xml.{Node, Text, XML}

object JooqCodegenPlugin extends AutoPlugin {

  override def requires: Plugins = JooqPlugin && Slf4jSimplePlugin

  object autoImport extends JooqCodegenKeys with CodegenConfig.Implicits {

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
    libraryDependencies ++= {
      val javaVersion = javaHome.value.map(parseJavaVersion).orElse(sys.props.get("java.version"))
      if (!javaVersion.forall(isJAXBBundled)) Seq(
        "javax.activation" % "activation" % "1.1.1" % JooqCodegen,
        "com.sun.xml.bind" % "jaxb-core" % "2.3.0.1" % JooqCodegen,
        "com.sun.xml.bind" % "jaxb-impl" % "2.3.1" % JooqCodegen
      )
      else Nil
    }
  ) ++
    JooqPlugin.jooqScopedSettings(JooqCodegen) ++
    inConfig(JooqCodegen)(Defaults.configSettings ++
      Seq(
        jooqModules := Seq("jooq-codegen")
      ) ++
      inTask(run)(Seq(
        fork := true,
        mainClass := Some(CrossVersion.partialVersion((JooqCodegen / jooqVersion).value) match {
          case Some((x, y)) if x < 3 || x == 3 && y < 11 => "org.jooq.util.GenerationTool"
          case _ => "org.jooq.codegen.GenerationTool"
        }),
        javaOptions ++= {
          val javaVersion = javaHome.value.map(parseJavaVersion).orElse(sys.props.get("java.version"))
          if (javaVersion.exists(jv => isJigsawEnabled(jv) && isJAXBBundled(jv)))
            Seq("--add-modules", "java.xml.bind")
          else
            Nil
        }
      ))) ++
    Slf4jSimplePlugin.slf4jSimpleScopedSettings(JooqCodegen) ++
    inConfig(JooqCodegen)(Seq(
      slf4jSimpleLogFile := "System.out",
      slf4jSimpleCacheOutputStream := true,
      slf4jSimpleShowThreadName := false,
      slf4jSimpleShowLogName := false,
      slf4jSimpleLevelInBrackets := true
    ))

  def jooqCodegenScopedSettings(config: Configuration): Seq[Setting[_]] = Seq(
    libraryDependencies ++= {
      if (!sys.props.get("java.version").forall(isJavaxAnnotationBundled))
        Seq("javax.annotation" % "javax.annotation-api" % "1.3.2" % config)
      else
        Nil
    }
  ) ++ inConfig(config)(Seq(
    jooqCodegen := codegenTask.value,
    jooqCodegen / skip := jooqCodegenConfig.?.value.isEmpty,
    jooqCodegenKeys := sys.env,
    jooqCodegenKeys ++= Seq(baseDirectory, config / sourceManaged),
    jooqCodegenSubstitutions := substitutionsTask(config).value,
    jooqCodegenConfigTransformer := configTransformerTask.value,
    jooqCodegenTransformedConfig := configTransformTask.value,
    jooqCodegenStrategy := CodegenStrategy.IfAbsent,
    sourceGenerators += autoCodegenTask.taskValue,
    jooqCodegenGeneratedSources / includeFilter := "*.java" | "*.scala",
    jooqCodegenGeneratedSources := jooqCodegenGeneratedSourcesFinder.value.get,
    jooqCodegenGeneratedSourcesFinder := generatedSourcesFinderTask.value,
    javacOptions ++= {
      if (sys.props.get("java.version").exists(jv => isJigsawEnabled(jv) && isJavaxAnnotationBundled(jv)))
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
      case CodegenKey.Setting(s) => keys(s, config).map(k => task(k -> (extracted.get(scope(s) / s): Any)))
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
        ClasspathLoader.using((JooqCodegen / fullClasspath).value) { loader =>
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
      (jooqCodegenGeneratedSources / includeFilter).value,
      (jooqCodegenGeneratedSources / excludeFilter).value)
  }

}
