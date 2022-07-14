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

import org.wartremover.{WartTraverser, WartUniverse}

object PlainSQL extends WartTraverser:

  def apply(u: WartUniverse): u.Traverser =
    new u.Traverser(this):
      import q.reflect.*

      val plainSQL = TypeTree.of[org.jooq.PlainSQL].symbol
      val allow = TypeTree.of[org.jooq.Allow.PlainSQL].symbol

      def hasAnnotation(s: Symbol, a: Symbol) = s.getAnnotation(a).isDefined

      override def traverseTree(tree: Tree)(owner: Symbol): Unit =
        tree match
          // Ignore trees marked by SuppressWarnings
          case t if hasWartAnnotation(t) =>

          // Ignore trees marked by Allow.PlainSQL
          case t: Definition if hasAnnotation(t.symbol, allow) =>

          case t: Select if hasAnnotation(t.symbol, plainSQL) =>
            error(tree.pos, "Plain SQL usage not allowed at current scope. Use @Allow.PlainSQL.")

          case _ =>
            super.traverseTree(tree)(owner)
