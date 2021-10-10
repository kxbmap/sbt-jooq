package sbtjooq.codegen

import org.scalactic.Prettifier
import org.scalactic.source.Position
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.Assertion
import scala.xml.Node
import scala.xml.Utility.trim

abstract class UnitSpec extends AnyWordSpec {

  def assertXML(expected: Node)(actual: Node)(implicit prettifier: Prettifier, pos: Position): Assertion =
    assertResult(trim(expected).toString())(trim(actual).toString())

}
