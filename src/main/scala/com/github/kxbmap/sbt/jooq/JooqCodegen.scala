package com.github.kxbmap.sbt.jooq

import sbt.Attributed.data
import sbt.Keys._
import sbt._
import sbt.plugins.JvmPlugin
import scala.xml.transform.{RewriteRule, RuleTransformer}
import scala.xml.{Elem, XML}

object JooqCodegen extends AutoPlugin {

  val DefaultJooqVersion = "3.6.2"
  val CodegenMainClass = "org.jooq.util.GenerationTool"

  override def requires: Plugins = JvmPlugin

  override lazy val projectSettings: Seq[Setting[_]] = jooqCodegenCommonSettings ++ jooqCodegenSettingsIn(Compile)

  val autoImport = JooqKeys

  import autoImport._

  private val forkOptions = taskKey[ForkOptions]("fork options")

  private lazy val jooqCodegenCommonSettings: Seq[Setting[_]] = Seq(
    jooqVersion := DefaultJooqVersion,
    ivyConfigurations += jooq,
    libraryDependencies ++= Seq(
      "org.jooq" % "jooq-codegen" % jooqVersion.value % jooq,
      "org.slf4j" % "slf4j-simple" % "1.7.12" % jooq
    )
  ) ++ inConfig(jooq)(Seq(
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

  def jooqCodegenSettingsIn(c: Configuration): Seq[Setting[_]] = inConfig(c)(Seq(
    jooqCodegen <<= codegenTask,
    jooqCodegenTargetDirectory <<= sourceManaged,
    jooqCodegenConfigFile := None,
    jooqCodegenConfigRewriteRules <<= configRewriteRules,
    jooqCodegenConfig <<= configTask,
    sourceGenerators <+= codegenIfAbsentTask
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

  private def configTask = Def.task {
    jooqCodegenConfigFile.value.map { config =>
      val transformer = new RuleTransformer(jooqCodegenConfigRewriteRules.value: _*)
      transformer(IO.reader(config)(XML.load))
    }
  }

  private def codegenTask = Def.task {
    jooqCodegenConfig.value.toSeq.flatMap { config =>
      IO.withTemporaryFile("jooq-codegen-", ".xml") { file =>
        XML.save(file.getAbsolutePath, config, "UTF-8", xmlDecl = true)
        runCodegen((mainClass in jooq).value, file, (forkOptions in jooq).value)
      } match {
        case 0 => sourcesIn(packageDir(jooqCodegenTargetDirectory.value, config))
        case e => sys.error(s"jOOQ codegen failure: $e")
      }
    }
  }

  private def codegenIfAbsentTask = Def.taskDyn {
    jooqCodegenConfig.value.map { config =>
      sourcesIn(packageDir(jooqCodegenTargetDirectory.value, config))
    } match {
      case Some(fs) if fs.isEmpty => Def.task(jooqCodegen.value)
      case Some(fs)               => Def.task(fs)
      case None                   => Def.task(Seq.empty[File])
    }
  }

  private def sourcesIn(dir: File): Seq[File] = (dir ** ("*.java" || "*.scala")).get

  private def packageDir(target: File, config: xml.Node): File = {
    val p = config \ "generator" \ "target" \ "packageName"
    val r = """^\w+(\.\w+)*$""".r
    p.text.trim match {
      case t@r(_)  => t.split('.').foldLeft(target)(_ / _)
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
