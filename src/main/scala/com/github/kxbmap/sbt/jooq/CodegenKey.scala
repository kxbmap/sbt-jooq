package com.github.kxbmap.sbt.jooq

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

  private[jooq] case class Setting[A](key: SettingKey[A]) extends Entry[A]

  private[jooq] case class Task[A](task: sbt.Task[A]) extends Entry[A]

  private[jooq] case class Constant[A](key: String, value: A) extends Entry[A]

  private[jooq] case class Mapped[A, B](entry: Entry[A], f: A => B) extends Entry[B]

  private[jooq] case class Named[A](entry: Entry[A], key: String) extends Entry[A]


  def apply[A](key: SettingKey[A]): Entry[A] = Setting(key)

  def apply[A](key: TaskKey[A]): Entry[A] = macro MacroImpl.taskKeyToCodegenKey[A]

  def apply[A](key: String, value: A): Entry[A] = Constant(key, value)


  implicit def settingKeyToConfigKey[A](key: SettingKey[A]): CodegenKey = Setting(key)

  implicit def taskKeyToConfigKey[A](key: TaskKey[A]): CodegenKey = macro MacroImpl.taskKeyToCodegenKey[A]

  implicit def constantToConfigKey[A](kv: (String, A)): CodegenKey = Constant(kv._1, kv._2)


  def taskValue[A](task: sbt.Task[A]): Entry[A] = Task(task)

  class MacroImpl(val c: blackbox.Context) {
    import c.universe._

    def taskKeyToCodegenKey[A: WeakTypeTag](key: Tree): Tree =
      q"_root_.com.github.kxbmap.sbt.jooq.CodegenKey.taskValue[${weakTypeOf[A]}]($key.taskValue)"
  }


  implicit def constantsSeqToConfigKeys[A](constants: Seq[(String, A)]): Seq[CodegenKey] =
    constants.map(constantToConfigKey)

  implicit def constantsMapToConfigKeys[A](constants: Map[String, A]): Seq[CodegenKey] =
    constants.map(constantToConfigKey).toSeq


  implicit def appendMapValues[A]: Append.Values[Seq[CodegenKey], Map[String, A]] =
    _ ++ _.map(constantToConfigKey)

}
