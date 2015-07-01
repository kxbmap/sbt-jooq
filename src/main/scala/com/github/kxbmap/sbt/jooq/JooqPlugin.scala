package com.github.kxbmap.sbt.jooq

import sbt.Attributed.data
import sbt.Keys._
import sbt._
import sbt.plugins.JvmPlugin
import scala.xml.transform.{RewriteRule, RuleTransformer}
import scala.xml.{Elem, XML}

object JooqPlugin extends AutoPlugin {

  val DefaultJooqVersion = "3.6.2"
  val CodegenMainClass = "org.jooq.util.GenerationTool"

  override def requires: Plugins = JvmPlugin

  override def projectSettings: Seq[Setting[_]] = jooqCodegenSettings

  object autoImport {
    lazy val jooq = config("jooq").hide

    lazy val codegen = taskKey[Seq[File]]("Run jOOQ codegen")
    lazy val jooqVersion = settingKey[String]("jOOQ version")
    lazy val jooqConfigFile = settingKey[File]("jOOQ codegen configuration file")
    lazy val jooqConfigRewriteRules = settingKey[Seq[RewriteRule]]("jOOQ codegen configuration rewrite rules")
    lazy val jooqConfig = taskKey[xml.Node]("jOOQ codegen configuration")
  }

  import autoImport._

  private lazy val jooqCodegenSettings: Seq[Setting[_]] = Seq(
    jooqVersion := DefaultJooqVersion,
    jooqConfigRewriteRules <<= rewriteRules,
    jooqConfig <<= configurationTask,
    ivyConfigurations += jooq,
    sourceGenerators in Compile += (codegen in jooq).taskValue,
    managedSourceDirectories in Compile += (sourceManaged in jooq).value,
    libraryDependencies ++= Seq(
      "org.jooq" % "jooq" % jooqVersion.value, // add to compile scope
      "org.jooq" % "jooq-codegen" % jooqVersion.value % jooq,
      "org.jooq" % "jooq-meta" % jooqVersion.value % jooq,
      "org.slf4j" % "slf4j-simple" % "1.7.12" % jooq
    )
  ) ++ inConfig(jooq)(Seq(
    codegen <<= codegenTask,
    sourceManaged <<= Defaults.configSrcSub(sourceManaged),
    managedClasspath := Classpaths.managedJars(jooq, classpathTypes.value, update.value),
    mainClass := Some(CodegenMainClass),
    javaOptions ++= Seq(
      "-classpath", Path.makeString(data(managedClasspath.value)),
      "-Dorg.slf4j.simpleLogger.logFile=System.out",
      "-Dorg.slf4j.simpleLogger.showLogName=false",
      "-Dorg.slf4j.simpleLogger.levelInBrackets=true"
    )
  ))

  def rewriteRule(name0: String)(f: PartialFunction[xml.Node, Seq[xml.Node]]): RewriteRule = new RewriteRule {
    override val name: String = name0
    override def transform(n: xml.Node): Seq[xml.Node] = f.applyOrElse(n, (_: xml.Node) => n)
  }

  private def rewriteRules = Def.setting {
    def directory = <directory>{(sourceManaged in jooq).value}</directory>
    Seq(
      rewriteRule("replaceTargetDirectory") {
        case Elem(_, "directory", _, _, _) => directory
        case e: Elem if e.label == "target" && e.child.forall(_.label != "directory") => e.copy(child = e.child :+ directory)
      }
    )
  }

  private def configurationTask = Def.task {
    val transformer = new RuleTransformer(jooqConfigRewriteRules.value: _*)
    transformer(IO.reader(jooqConfigFile.value)(XML.load))
  }

  private def codegenTask = Def.task {
    val exitValue = IO.withTemporaryFile("jooq-codegen", ".xml") { file =>
      XML.save(file.getAbsolutePath, jooqConfig.value, "UTF-8", xmlDecl = true)
      runCodegen(mainClass.value, file, forkOptions.value)
    }
    if (exitValue != 0) {
      sys.error(s"jOOQ codegen failure: $exitValue")
    }
    (sourceManaged.value ** ("*.java" || "*.scala")).get
  }

  private def forkOptions = Def.task {
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

  private def runCodegen(mainClass: Option[String], config: File, forkOptions: ForkOptions): Int = {
    val main = mainClass.getOrElse(CodegenMainClass)
    val args = Seq(config.getAbsolutePath)
    val process = Fork.java.fork(forkOptions, main +: args)
    try
      process.exitValue()
    catch {
      case _: InterruptedException =>
        process.destroy()
        1
    }
  }

}
