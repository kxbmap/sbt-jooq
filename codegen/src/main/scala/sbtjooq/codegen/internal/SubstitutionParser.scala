package sbtjooq.codegen.internal

import fastparse._, NoWhitespace._
import sbt.Command

class SubstitutionParser(vars: Map[String, Any]) {

  private def Substitution[_: P]: P[String] =
    P("{" ~/
      CharsWhile(_ != '}').!
        .map(_.trim)
        .flatMap(vars.get(_).map(_.toString).fold[P[String]](Fail)(Pass(_)))
        .opaque("VariableName") ~/ "}")

  private def Text[_: P]: P[String] =
    P((("$" ~/ ("$".! | Substitution) | CharsWhile(_ != '$').!).rep ~ End).map(_.mkString))

  def parse(text: String): Either[String, String] =
    fastparse.parse(text, Text(_)).fold(
      (_, n, e) => {
        val t = e.trace()
        Left(t.label match {
          case "VariableName" =>
            val invalid = e.input.slice(n, e.input.length).takeWhile(_ != '}').trim
            val similar = Command.similar(invalid, vars.keys)
            s"No variables found for $invalid$similar"
          case _ => t.msg
        })
      },
      (s, _) => Right(s)
    )

}
