package com.github.kxbmap.sbt.jooq

import sbt.Attributed.data
import sbt.Keys._
import sbt._
import sbt.plugins.JvmPlugin
import scala.xml.transform.{RewriteRule, RuleTransformer}
import scala.xml.{Elem, XML}

object JooqCodegen extends AutoPlugin {

  val DefaultJooqVersion = "3.9.2"

  override def requires: Plugins = JvmPlugin

  override def projectSettings: Seq[Setting[_]] = jooqCodegenSettings

  object autoImport {

    val jooq = config("jooq").hide

    val jooqVersion = settingKey[String]("jOOQ version")
    val jooqCodegen = taskKey[Seq[File]]("Run jOOQ codegen")
    val jooqCodegenConfigFile = settingKey[Option[File]]("jOOQ codegen configuration file")
    val jooqCodegenTargetDirectory = settingKey[File]("jOOQ codegen target directory")
    val jooqCodegenConfigRewriteRules = settingKey[Seq[RewriteRule]]("jOOQ codegen configuration rewrite rules")
    val jooqCodegenConfig = taskKey[xml.Node]("jOOQ codegen configuration")
    val jooqCodegenStrategy = settingKey[CodegenStrategy]("jOOQ codegen strategy")

    val CodegenStrategy = com.github.kxbmap.sbt.jooq.CodegenStrategy

  }

  import autoImport.{CodegenStrategy => _, _}

  private val forkOptions = taskKey[ForkOptions]("fork options")

  private lazy val jooqCodegenSettings: Seq[Setting[_]] = Seq(
    jooqVersion := DefaultJooqVersion,
    jooqCodegen := codegenTask.value,
    jooqCodegenConfigFile := None,
    jooqCodegenTargetDirectory := (sourceManaged in Compile).value,
    jooqCodegenConfigRewriteRules := configRewriteRules.value,
    jooqCodegenConfig := codegenConfigTask.value,
    jooqCodegenStrategy := CodegenStrategy.IfAbsent,
    sourceGenerators in Compile += autoCodegenTask.taskValue,
    ivyConfigurations += jooq,
    libraryDependencies ++= Seq(
      "org.jooq" % "jooq" % jooqVersion.value, // add to compile scope
      "org.jooq" % "jooq-codegen" % jooqVersion.value % jooq,
      "org.slf4j" % "slf4j-simple" % "1.7.18" % jooq
    )
  ) ++ inConfig(jooq)(Seq(
    managedClasspath := Classpaths.managedJars(jooq, classpathTypes.value, update.value),
    mainClass := Some("org.jooq.util.GenerationTool"),
    javaOptions ++= Seq(
      "-classpath", Path.makeString(data(managedClasspath.value)),
      "-Dorg.slf4j.simpleLogger.logFile=System.out",
      "-Dorg.slf4j.simpleLogger.showLogName=false",
      "-Dorg.slf4j.simpleLogger.levelInBrackets=true"
    ),
    forkOptions := forkOptionsTask.value
  ))


  def rewriteRule(name0: String)(f: PartialFunction[xml.Node, Seq[xml.Node]]): RewriteRule = new RewriteRule {
    override val name: String = name0
    override def transform(n: xml.Node): Seq[xml.Node] = f.applyOrElse(n, (_: xml.Node) => n)
  }

  private def configRewriteRules = Def.setting {
    def directory = <directory>{jooqCodegenTargetDirectory.value}</directory>
    Seq(
      rewriteRule("replaceTargetDirectory") {
        case Elem(_, "directory", _, _, _) => directory
        case e: Elem if e.label == "target" && e.child.forall(_.label != "directory") => e.copy(child = e.child :+ directory)
      }
    )
  }

  private def codegenConfigTask = Def.task {
    val base = baseDirectory.value
    val file = jooqCodegenConfigFile.value.getOrElse(sys.error("required: jooqCodegenConfigFile or jooqCodegenConfig"))
    val transformer = new RuleTransformer(jooqCodegenConfigRewriteRules.value: _*)
    transformer(IO.reader(IO.resolve(base, file))(XML.load))
  }

  private def codegenTask = Def.task {
    val config = jooqCodegenConfig.value
    IO.withTemporaryFile("jooq-codegen-", ".xml") { file =>
      val main = (mainClass in jooq).value.getOrElse(sys.error("required: mainClass in jooq"))
      XML.save(file.getAbsolutePath, config, "UTF-8", xmlDecl = true)
      runCodegen(main, file, (forkOptions in jooq).value)
    } match {
      case 0 => sourcesIn(packageDir(jooqCodegenTargetDirectory.value, config))
      case e => sys.error(s"jOOQ codegen failure: $e")
    }
  }

  private def autoCodegenTask = Def.taskDyn {
    jooqCodegenStrategy.value match {
      case CodegenStrategy.Always => Def.task(jooqCodegen.value)
      case CodegenStrategy.IfAbsent =>
        Def.taskDyn {
          val fs = sourcesIn(packageDir(jooqCodegenTargetDirectory.value, jooqCodegenConfig.value))
          if (fs.isEmpty)
            Def.task(jooqCodegen.value)
          else
            Def.task(fs)
        }
    }
  }

  private def sourcesIn(dir: File): Seq[File] = (dir ** ("*.java" || "*.scala")).get

  private def packageDir(target: File, config: xml.Node): File = {
    val p = config \ "generator" \ "target" \ "packageName"
    val r = """^\w+(\.\w+)*$""".r
    p.text.trim match {
      case t@r(_) => t.split('.').foldLeft(target)(_ / _)
      case invalid => sys.error(s"invalid packageName format: $invalid")
    }
  }

  private def forkOptionsTask = Def.task {
    ForkOptions(
      javaHome = javaHome.value,
      outputStrategy = outputStrategy.value,
      bootJars = Nil,
      workingDirectory = Some(baseDirectory.value),
      runJVMOptions = javaOptions.value,
      connectInput = connectInput.value,
      envVars = envVars.value
    )
  }

  private def runCodegen(mainClass: String, config: File, forkOptions: ForkOptions): Int = {
    val process = Fork.java.fork(forkOptions, Seq(mainClass, config.getAbsolutePath))
    try
      process.exitValue()
    catch {
      case _: InterruptedException =>
        process.destroy()
        1
    }
  }

}
