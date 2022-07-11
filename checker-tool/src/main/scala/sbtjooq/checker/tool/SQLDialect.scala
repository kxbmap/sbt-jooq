package sbtjooq.checker.tool

import org.jooq.{Allow, Require, Support, SQLDialect as Dialect}
import org.wartremover.{WartTraverser, WartUniverse}
import scala.annotation.tailrec

object SQLDialect extends WartTraverser {

  private[this] val dialects = Dialect.values().map(d => d.name() -> d).toMap
  private[this] val families = Dialect.families().toSet

  def apply(u: WartUniverse): u.Traverser = {
    import u.universe.*

    val Support = typeOf[Support]
    val Allow = typeOf[Allow]
    val Require = typeOf[Require]

    def getAnnotation(s: Symbol, a: Type): Option[Annotation] =
      s.annotations.find(_.tree.tpe =:= a)

    def getDialects(a: Annotation, defaults: Set[Dialect] = Set.empty): Set[Dialect] =
      a.tree.children.tail match {
        case Nil => defaults
        case value :: _ =>
          value // value =
            .children.tail.head // Array(...)
            .children.tail // arguments
            .map(x => dialects(show(x)))
            .toSet
      }

    def showDialects(xs: Iterable[Dialect]) = xs.toSeq.sorted.mkString("[", ", ", "]")

    @tailrec
    def collectAllowed(owners: List[Symbol], acc: Set[Dialect] = Set.empty): Set[Dialect] =
      owners match {
        case Nil => acc
        case x :: xs =>
          val allows = getAnnotation(x, Allow).toSet.flatMap(getDialects(_, families))
          collectAllowed(xs, acc ++ allows)
      }

    @tailrec
    def collectRequired(owners: List[Symbol]): Set[Dialect] =
      owners match {
        case Nil => Set.empty
        case x :: xs =>
          getAnnotation(x, Require) match {
            case Some(r) => getDialects(r)
            case None => collectRequired(xs)
          }
      }

    new Traverser {
      private[this] var owners: List[Symbol] = Nil

      override def traverse(tree: Tree): Unit =
        tree match {
          // Ignore trees marked by SuppressWarnings
          case t if hasWartAnnotation(u)(t) =>

          case t: Select =>
            getAnnotation(t.symbol, Support).foreach { s =>
              val supported = getDialects(s, families)
              (for {
                _ <- checkAllowed(supported)
                _ <- checkRequired(supported)
              } yield ()).swap.foreach(error(u)(tree.pos, _))
            }
            super.traverse(tree)

          case _ =>
            super.traverse(tree)
        }

      private def checkAllowed(supported: Set[Dialect]): Either[String, Unit] = {
        val allowed = collectAllowed(owners)
        if (allowed.isEmpty)
          Left("No jOOQ API usage is allowed at current scope. Use @Allow.")
        else if (allowed.exists(a => supported.exists(a.supports)))
          Right(())
        else
          Left(
            s"The allowed dialects in scope ${showDialects(allowed)} do not include any of the supported dialects: ${showDialects(supported)}"
          )
      }

      private def checkRequired(supported: Set[Dialect]): Either[String, Unit] = {
        val required = collectRequired(owners)
        if (required.exists(r => supported.forall(!r.supports(_))))
          Left(
            s"Not all of the required dialects ${showDialects(required)} from the current scope are supported ${showDialects(supported)}"
          )
        else
          Right(())
      }

      override def atOwner(owner: Symbol)(traverse: => Unit): Unit = {
        owners ::= owner
        super.atOwner(owner)(traverse)
        owners = owners.tail
      }
    }
  }

}
