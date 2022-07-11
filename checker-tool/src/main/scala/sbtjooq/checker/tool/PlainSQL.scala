package sbtjooq.checker.tool

import org.wartremover.{WartTraverser, WartUniverse}

object PlainSQL extends WartTraverser {

  def apply(u: WartUniverse): u.Traverser = {
    import u.universe.*

    val plainSQL = typeOf[org.jooq.PlainSQL]
    val allow = typeOf[org.jooq.Allow.PlainSQL]

    def hasAnnotation(s: Symbol, a: Type) = s.annotations.exists(_.tree.tpe =:= a)

    new Traverser {
      override def traverse(tree: Tree): Unit =
        tree match {
          // Ignore trees marked by SuppressWarnings
          case t if hasWartAnnotation(u)(t) =>

          // Ignore trees marked by Allow.PlainSQL
          case t: MemberDef if hasAnnotation(t.symbol, allow) =>

          case t: Select if hasAnnotation(t.symbol, plainSQL) =>
            error(u)(tree.pos, "Plain SQL usage not allowed at current scope. Use @Allow.PlainSQL.")

          case _ =>
            super.traverse(tree)
        }
    }
  }

}
