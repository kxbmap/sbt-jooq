/*
 * Copyright 2015 Tsukasa Kitachi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package sbtjooq.checker.tool

import org.jooq.{Allow, Require, Support, SQLDialect as Dialect}
import org.wartremover.{WartTraverser, WartUniverse}
import scala.annotation.tailrec

object SQLDialect extends WartTraverser:

  private[this] val dialects = Dialect.values().map(d => d.name() -> d).toMap
  private[this] val families = Dialect.families().toSet

  override def apply(u: WartUniverse): u.Traverser =
    new u.Traverser(this):
      import q.reflect.*

      private val Support = TypeTree.of[Support].symbol
      private val Allow = TypeTree.of[Allow].symbol
      private val Require = TypeTree.of[Require].symbol

      private def getDialects(a: Term, defaults: Set[Dialect] = Set.empty): Set[Dialect] =
        a match
          case Apply(
                Select(_, "<init>"),
                Apply(Apply(_, Typed(Repeated(values, _), _) :: Nil), Apply(_, _ :: Nil) :: Nil) :: Nil,
              ) =>
            val ds = values.collect {
              case Ident(x) => x
              case Select(_, x) => x
            }
            ds.map(dialects).toSet

          case _ => defaults

      private def showDialects(xs: Iterable[Dialect]): String =
        xs.toSeq.sorted.mkString("[", ", ", "]")

      private[this] var owners: List[Symbol] = Nil

      override def traverseTree(tree: Tree)(owner: Symbol): Unit =
        owners ::= owner
        tree match
          // Ignore trees marked by SuppressWarnings
          case t if hasWartAnnotation(t) =>

          case t: Select =>
            t.symbol.getAnnotation(Support).foreach { s =>
              val supported = getDialects(s, families)
              val checked = for
                _ <- checkAllowed(supported)
                _ <- checkRequired(supported)
              yield ()
              checked.swap.foreach(error(tree.pos, _))
            }
            super.traverseTree(tree)(owner)

          case _ =>
            super.traverseTree(tree)(owner)
        owners = owners.tail

      private def checkAllowed(supported: Set[Dialect]): Either[String, Unit] =
        val allowed = collectAllowed(owners)
        if allowed.isEmpty then Left("No jOOQ API usage is allowed at current scope. Use @Allow.")
        else if allowed.exists(a => supported.exists(a.supports)) then Right(())
        else
          Left(
            s"The allowed dialects in scope ${showDialects(allowed)} do not include any of the supported dialects: ${showDialects(supported)}"
          )

      @tailrec
      private def collectAllowed(owners: List[Symbol], acc: Set[Dialect] = Set.empty): Set[Dialect] =
        owners match
          case Nil => acc
          case x :: xs =>
            val allows = x.getAnnotation(Allow).toSet.flatMap(getDialects(_, families))
            collectAllowed(xs, acc ++ allows)

      private def checkRequired(supported: Set[Dialect]): Either[String, Unit] =
        val required = collectRequired(owners)
        if required.exists(r => supported.forall(!r.supports(_))) then
          Left(
            s"Not all of the required dialects ${showDialects(required)} from the current scope are supported ${showDialects(supported)}"
          )
        else Right(())

      @tailrec
      private def collectRequired(owners: List[Symbol]): Set[Dialect] =
        owners match
          case Nil => Set.empty
          case x :: xs =>
            x.getAnnotation(Require) match
              case Some(r) => getDialects(r)
              case None => collectRequired(xs)
