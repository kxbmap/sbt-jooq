package sbtjooq.codegen

import sbt._

class CodegenConfigTest extends UnitSpec {

  val key = settingKey[CodegenConfig]("")

  "From a file" when {
    ":=" should {
      "compiles" in {
        assertCompiles("""key := file("foo.xml")""")
      }
    }
    "+=" should {
      "compiles" in {
        assertCompiles("""key += file("foo.xml")""")
      }
    }
    "++=" should {
      "NOT compile" in {
        assertTypeError("""key ++= file("foo.xml")""")
      }
    }
  }

  "From files" when {
    ":=" should {
      "compiles" in {
        assertCompiles("""key := Seq(file("foo.xml"), file("bar.xml"))""")
      }
    }
    "+=" should {
      "NOT compiles" in {
        assertTypeError("""key += Seq(file("foo.xml"), file("bar.xml"))""")
      }
    }
    "++=" should {
      "compiles" in {
        assertCompiles("""key ++= Seq(file("foo.xml"), file("bar.xml"))""")
      }
    }
  }

  "From a URI" when {
    ":=" should {
      "compiles" in {
        assertCompiles("""key := uri("classpath:foo.xml")""")
      }
    }
    "+=" should {
      "compiles" in {
        assertCompiles("""key += uri("classpath:foo.xml")""")
      }
    }
    "++=" should {
      "NOT compile" in {
        assertTypeError("""key ++= uri("classpath:foo.xml")""")
      }
    }
  }

  "From URIs" should {
    ":=" should {
      "compiles" in {
        assertCompiles("""key := Seq(uri("classpath:foo.xml"), uri("classpath:bar.xml"))""")
      }
    }
    "+=" should {
      "NOT compile" in {
        assertTypeError("""key += Seq(uri("classpath:foo.xml"), uri("classpath:bar.xml"))""")
      }
    }
    "++=" should {
      "compiles" in {
        assertCompiles("""key ++= Seq(uri("classpath:foo.xml"), uri("classpath:bar.xml"))""")
      }
    }
  }

  "From a XML" when {
    ":=" should {
      "compiles" in {
        assertCompiles("""key := <configuration></configuration>""")
      }
    }
    "+=" should {
      "compiles" in {
        assertCompiles("""key += <configuration></configuration>""")
      }
    }
    "++=" should {
      "NOT compile" in {
        assertTypeError("""key ++= <configuration></configuration>""")
      }
    }
  }

  "From Seq of XML" when {
    ":=" should {
      "compiles" in {
        assertCompiles("""key := Seq(<configuration>1</configuration>, <configuration>2</configuration>)""")
      }
    }
    "+=" should {
      "NOT compile" in {
        assertTypeError("""key += Seq(<configuration>1</configuration>, <configuration>2</configuration>)""")
      }
    }
    "++=" should {
      "compiles" in {
        assertCompiles("""key ++= Seq(<configuration>1</configuration>, <configuration>2</configuration>)""")
      }
    }
  }

  "From XMLs by literal" when {
    ":=" should {
      "compiles" in {
        assertCompiles(
          """key :=
            |  <configuration>1</configuration>
            |  <configuration>2</configuration>
            |  <configuration>3</configuration>
            |""".stripMargin
        )
      }
    }
    "+=" should {
      "NOT compile" in {
        assertTypeError(
          """key +=
            |  <configuration>1</configuration>
            |  <configuration>2</configuration>
            |  <configuration>3</configuration>
            |""".stripMargin
        )
      }
    }
    "++=" should {
      "compiles" in {
        assertCompiles(
          """key ++=
            |  <configuration>1</configuration>
            |  <configuration>2</configuration>
            |  <configuration>3</configuration>
            |""".stripMargin
        )
      }
    }
  }

  "From Seq of mixed type values" when {
    ":=" should {
      "compiles" in {
        assertCompiles(
          """key := Seq[CodegenConfig](
            |  file("foo.xml"),
            |  uri("classpath:foo.xml"),
            |  <configuration></configuration>,
            |)
            |""".stripMargin
        )
      }
    }
    "+=" should {
      "NOT compile" in {
        assertTypeError(
          """key += Seq[CodegenConfig](
            |  file("foo.xml"),
            |  uri("classpath:foo.xml"),
            |  <configuration></configuration>
            |)""".stripMargin
        )
      }
    }
    "++=" should {
      "compiles" in {
        assertCompiles(
          """key ++= Seq[CodegenConfig](
            |  file("foo.xml"),
            |  uri("classpath:foo.xml"),
            |  <configuration></configuration>
            |)""".stripMargin
        )
      }
    }
  }

  "CodegenConfig" when {
    ":=" should {
      "compiles" in {
        assertCompiles(
          """val config: CodegenConfig = ???
            |key := config
            |""".stripMargin
        )
      }
    }
    "+=" should {
      "NOT compile" in {
        assertTypeError(
          """val config: CodegenConfig = ???
            |key += config
            |""".stripMargin
        )
      }
    }
    "++=" should {
      "compiles" in {
        assertCompiles(
          """val config: CodegenConfig = ???
            |key ++= config
            |""".stripMargin
        )
      }
    }
  }

  "CodegenConfig.Single" when {
    ":=" should {
      "compiles" in {
        assertCompiles(
          """val config: CodegenConfig.Single = ???
            |key := config
            |""".stripMargin
        )
      }
    }
    "+=" should {
      "compiles" in {
        assertCompiles(
          """val config: CodegenConfig.Single = ???
            |key += config
            |""".stripMargin
        )
      }
    }
    "++=" should {
      "compiles" in {
        assertCompiles(
          """val config: CodegenConfig.Single = ???
            |key ++= config
            |""".stripMargin
        )
      }
    }
  }

  "CodegenConfig.Sequence" when {
    ":=" should {
      "compiles" in {
        assertCompiles(
          """val config: CodegenConfig.Sequence = ???
            |key := config
            |""".stripMargin
        )
      }
    }
    "+=" should {
      "NOT compile" in {
        assertTypeError(
          """val config: CodegenConfig.Sequence = ???
            |key += config
            |""".stripMargin
        )
      }
    }
    "++=" should {
      "compiles" in {
        assertCompiles(
          """val config: CodegenConfig.Sequence = ???
            |key ++= config
            |""".stripMargin
        )
      }
    }
  }

  def dummy(): Unit = {
    <dummy></dummy>
  }
}
