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

  val autoImport = JooqKeys

  import autoImport._

  private val forkOptions = taskKey[ForkOptions]("fork options")

  private lazy val jooqCodegenSettings: Seq[Setting[_]] = Seq(
    jooqVersion := DefaultJooqVersion,
    jooqCodegen <<= codegenTask,
    jooqCodegenConfigRewriteRules <<= configRewriteRules,
    jooqCodegenConfig <<= configTask,
    ivyConfigurations += jooq,
    sourceGenerators in Compile <+= codegenIfAbsentTask,
    managedSourceDirectories in Compile += (sourceManaged in jooq).value,
    libraryDependencies ++= Seq(
      "org.jooq" % "jooq" % jooqVersion.value, // add to compile scope
      "org.jooq" % "jooq-codegen" % jooqVersion.value % jooq,
      "org.jooq" % "jooq-meta" % jooqVersion.value % jooq,
      "org.slf4j" % "slf4j-simple" % "1.7.12" % jooq
    )
  ) ++ inConfig(jooq)(Seq(
    sourceManaged <<= Defaults.configSrcSub(sourceManaged),
    managedClasspath := Classpaths.managedJars(jooq, classpathTypes.value, update.value),
    mainClass := Some(CodegenMainClass),
    javaOptions ++= Seq(
      "-classpath", Path.makeString(data(managedClasspath.value)),
      "-Dorg.slf4j.simpleLogger.logFile=System.out",
      "-Dorg.slf4j.simpleLogger.showLogName=false",
      "-Dorg.slf4j.simpleLogger.levelInBrackets=true"
    ),
    forkOptions <<= forkOptionsTask
  ))

  def rewriteRule(name0: String)(f: PartialFunction[xml.Node, Seq[xml.Node]]): RewriteRule = new RewriteRule {
    override val name: String = name0
    override def transform(n: xml.Node): Seq[xml.Node] = f.applyOrElse(n, (_: xml.Node) => n)
  }

  private def configRewriteRules = Def.setting {
    def directory = <directory>{(sourceManaged in jooq).value}</directory>
    Seq(
      rewriteRule("replaceTargetDirectory") {
        case Elem(_, "directory", _, _, _) => directory
        case e: Elem if e.label == "target" && e.child.forall(_.label != "directory") => e.copy(child = e.child :+ directory)
      }
    )
  }

  private def configTask = Def.task {
    val transformer = new RuleTransformer(jooqCodegenConfigRewriteRules.value: _*)
    transformer(IO.reader(jooqCodegenConfigFile.value)(XML.load))
  }

  private def codegenTask = Def.task {
    IO.withTemporaryFile("jooq-codegen-", ".xml") { file =>
      XML.save(file.getAbsolutePath, jooqCodegenConfig.value, "UTF-8", xmlDecl = true)
      runCodegen((mainClass in jooq).value, file, (forkOptions in jooq).value)
    } match {
      case 0 => sourcesIn((sourceManaged in jooq).value)
      case e => sys.error(s"jOOQ codegen failure: $e")
    }
  }

  private def codegenIfAbsentTask = Def.taskDyn {
    val fs = sourcesIn((sourceManaged in jooq).value)
    if (fs.isEmpty)
      Def.task(jooqCodegen.value)
    else
      Def.task(fs)
  }

  private def sourcesIn(dir: File): Seq[File] = (dir ** ("*.java" || "*.scala")).get

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
