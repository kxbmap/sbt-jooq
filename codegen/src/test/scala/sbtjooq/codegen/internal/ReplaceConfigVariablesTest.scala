package sbtjooq.codegen.internal

import sbtjooq.codegen.UnitSpec

class ReplaceConfigVariablesTest extends UnitSpec {

  "Codegen.configVariableTransformer" when {

    val vars = Map[String, Any](
      "STRING" -> "foo",
      "FILE" -> new java.io.File("xxx.txt"),
    )
    val transform = Codegen.replaceConfigVariables(vars)

    "configuration has no placeholders" should {
      "do nothing" in {
        val e = <configuration>BAR</configuration>
        assertXML(e)(transform(e))
      }
    }

    "configuration contains placeholder that refer String variable" should {
      "replace with string value" in {
        val x = <configuration>${{STRING}}bar</configuration>
        val e = <configuration>foobar</configuration>
        assertXML(e)(transform(x))
      }
    }

    "configuration contains placeholder that refer File variable" should {
      "replace with File.toString value" in {
        val x = <configuration>${{FILE}}</configuration>
        val e = <configuration>xxx.txt</configuration>
        assertXML(e)(transform(x))
      }
    }

    "configuration contains placeholders" should {
      "replace with each value" in {
        val x = <configuration>${{STRING}}${{FILE}}</configuration>
        val e = <configuration>fooxxx.txt</configuration>
        assertXML(e)(transform(x))
      }
    }

    "missing closing brace" should {
      "raise an error" in {
        val e = intercept[Exception] {
          transform(<configuration><generator>${{STRING</generator></configuration>)
        }
        val m = e.getMessage
        assert(m.contains("Missing closing brace") && m.contains("/configuration/generator"))
      }
    }

    "missing variable" should {
      "raise an error" in {
        val e = intercept[Exception] {
          transform(<configuration><generator>${{MISSING}}</generator></configuration>)
        }
        val m = e.getMessage
        assert(m.contains("No variables found") && m.contains("MISSING") && m.contains("/configuration/generator"))
      }
    }

    "escaped $$" should {
      "replace with $" in {
        val x = <configuration>$${{STRING}}$$${{FILE}}</configuration>
        val e = <configuration>${{STRING}}$xxx.txt</configuration>
        assertXML(e)(transform(x))
      }
    }

  }

}
