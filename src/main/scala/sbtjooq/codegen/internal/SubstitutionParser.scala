package sbtjooq.codegen.internal

import fastparse.all._
import sbt.Command

class SubstitutionParser(substitutions: Map[String, String]) {

  private val Substitution: P[String] =
    P("{" ~/
      CharsWhile(_ != '}').!
        .map(_.trim)
        .flatMap(substitutions.get(_).fold[P[String]](Fail)(PassWith))
        .opaque("SubstitutionKey") ~/ "}")

  private val Text: P[String] =
    P((("$" ~/ ("$".! | Substitution) | CharsWhile(_ != '$').!).rep ~ End).map(_.mkString))

  def parse(text: String): Either[String, String] =
    Text.parse(text).fold(
      (_, n, e) => Left(e.traced.expected match {
        case "SubstitutionKey" =>
          val invalid = e.input.slice(n, e.input.length).takeWhile(_ != '}').trim
          val similar = Command.similar(invalid, substitutions.keys)
          s"invalid substitution key: $invalid$similar"
        case expected =>
          val found = if (n < e.input.length) s""""${e.input.slice(n, n + 20)}"""" else "End"
          s"found: $found, expected: $expected"
      }),
      (s, _) => Right(s)
    )

}
