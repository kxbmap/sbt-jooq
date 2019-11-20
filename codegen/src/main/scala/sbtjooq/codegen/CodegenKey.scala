package sbtjooq.codegen

import sbt.Keys._
import sbt._
import scala.language.experimental.macros
import scala.language.implicitConversions
import scala.reflect.macros.blackbox

object CodegenKey {

  sealed trait Entry[+A] {

    def map[B](f: A => B): Entry[B] = this match {
      case Mapped(s, f0) => Mapped(s, f.compose(f0))
      case _ => Mapped(this, f)
    }

    def withName(key: String): Entry[A] = this match {
      case Named(e, _) => Named(e, key)
      case _ => Named(this, key)
    }

  }

  private case class Setting[A](key: SettingKey[A]) extends Entry[A]

  private case class TaskValue[A](task: Task[A]) extends Entry[A]

  private case class Constant[A](key: String, value: A) extends Entry[A]

  private case class Mapped[A, B](entry: Entry[A], f: A => B) extends Entry[B]

  private case class Named[A](entry: Entry[A], key: String) extends Entry[A]


  def apply[A](key: SettingKey[A]): Entry[A] = Setting(key)

  def apply[A](key: TaskKey[A]): Entry[A] = macro MacroImpl.taskKeyToCodegenKey[A]

  def apply[A](key: String, value: A): Entry[A] = Constant(key, value)


  implicit def settingKeyToConfigKey[A](key: SettingKey[A]): CodegenKey = Setting(key)

  implicit def taskKeyToConfigKey[A](key: TaskKey[A]): CodegenKey = macro MacroImpl.taskKeyToCodegenKey[A]

  implicit def constantToConfigKey[A](kv: (String, A)): CodegenKey = Constant(kv._1, kv._2)


  def taskValue[A](task: Task[A]): Entry[A] = TaskValue(task)

  class MacroImpl(val c: blackbox.Context) {
    import c.universe._

    def taskKeyToCodegenKey[A: WeakTypeTag](key: Tree): Tree =
      q"_root_.sbtjooq.codegen.CodegenKey.taskValue[${weakTypeOf[A]}]($key.taskValue)"
  }


  implicit def constantsSeqToConfigKeys[A](constants: Seq[(String, A)]): Seq[CodegenKey] =
    constants.map(constantToConfigKey)

  implicit def constantsMapToConfigKeys[A](constants: Map[String, A]): Seq[CodegenKey] =
    constants.map(constantToConfigKey).toSeq


  implicit def appendMapValues[A]: Append.Values[Seq[CodegenKey], Map[String, A]] =
    _ ++ _.map(constantToConfigKey)



  def build(
      codegenKeys: Seq[CodegenKey],
      config: Configuration,
      state: State,
      thisProject: ProjectRef): Task[Map[String, String]] = {

    val extracted = Project.extract(state)

    def scope(scoped: Scoped): Scope = {
      val scope0 = scoped.scope
      if (scope0.project == This) scope0 in thisProject else scope0
    }

    def entry(key: CodegenKey): Seq[Task[(String, Any)]] = key match {
      case CodegenKey.Setting(s) => keys(s, config).map(k => task(k -> (extracted.get(scope(s) / s): Any)))
      case CodegenKey.TaskValue(t) => keys(t, config).map(k => t.map(v => k -> (v: Any)))
      case CodegenKey.Constant(k, v) => Seq(task(k -> v))
      case CodegenKey.Mapped(e, f) => entry(e).map(_.map { case (k, v) => k -> (f(v): Any) })
      case CodegenKey.Named(e, k) => entry(e).map(_.map { case (_, v) => k -> v })
    }

    codegenKeys.flatMap(entry)
      .map(_.map { case (k, v) => k -> v.toString })
      .join
      .map(_.toMap)
  }

  private def keys(scoped: Scoped, config: Configuration): Seq[String] = keys(scoped.scope, scoped.key, config)

  private def keys(scoped: ScopedKey[_], config: Configuration): Seq[String] = keys(scoped.scope, scoped.key, config)

  private def keys(task: Task[_], config: Configuration): Seq[String] =
    task.info.name.map(Seq(_))
      .orElse(task.info.attributes.get(taskDefinitionKey).map(keys(_, config)))
      .getOrElse(throw new MessageOnlyException("anonymous task"))

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

}
