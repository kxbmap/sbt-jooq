package sbtjooq.codegen.internal

import sbtjooq.codegen.UnitSpec
import scala.xml.Utility.trim

class CodegenTest extends UnitSpec {

  "appendGeneratorTargetDirectory" when {

    val appendTo = Codegen.appendGeneratorTargetDirectory(new java.io.File("foo"))

    "generator target directory is non empty" should {
      "do nothing" in {
        val e = <configuration><generator><target><directory>bar</directory></target></generator></configuration>
        val x = appendTo(e)
        assert(trim(x) == trim(e))
      }
    }

    "configuration element is empty" should {
      "append generator target directory element" in {
        val x = appendTo(<configuration/>)
        val e = <configuration><generator><target><directory>foo</directory></target></generator></configuration>
        assert(trim(x) == trim(e))
      }
    }

    "generator element is empty" should {
      "append target directory element" in {
        val x = appendTo(<configuration><generator/></configuration>)
        val e = <configuration><generator><target><directory>foo</directory></target></generator></configuration>
        assert(trim(x) == trim(e))
      }
    }

    "target element is empty" should {
      "append directory element" in {
        val x = appendTo(<configuration><generator><target/></generator></configuration>)
        val e = <configuration><generator><target><directory>foo</directory></target></generator></configuration>
        assert(trim(x) == trim(e))
      }
    }

    "directory element is empty" should {
      "append file name" in {
        val x = appendTo(<configuration><generator><target><directory/></target></generator></configuration>)
        val e = <configuration><generator><target><directory>foo</directory></target></generator></configuration>
        assert(trim(x) == trim(e))
      }
    }

    "target element has only packageName element" should {
      "append directory element" in {
        val x = appendTo(
          <configuration>
            <generator>
              <target>
                <packageName>com.example</packageName>
              </target>
            </generator>
          </configuration>
        )
        val e =
          <configuration>
            <generator>
              <target>
                <packageName>com.example</packageName>
                <directory>foo</directory>
              </target>
            </generator>
          </configuration>
        assert(trim(x) == trim(e))
      }
    }
  }


  "generatorTargetPackage" when {

    def config(name: String) =
      <configuration>
        <generator><target><packageName>{name}</packageName></target></generator>
      </configuration>

    "packageName is blank" should {
      "returns default package" in {
        val pkg = Codegen.generatorTargetPackage(config("  "))
        assert(pkg == Seq("org", "jooq", "generated"))
      }
    }

    "packageName starts with not UnicodeIdentifierStart character" should {
      "prepend underscore to the name" in {
        val pkg = Codegen.generatorTargetPackage(config("42"))
        assert(pkg == Seq("_42"))
      }
    }

    "packageName contains non UnicodeIdentifierPart but ASCII characters" should {
      "replace with _xx" in {
        val pkg = Codegen.generatorTargetPackage(config("@"))
        assert(pkg == Seq("_40"))
      }
    }

    "packageName contains non UnicodeIdentifierPart and non ASCII characters" should {
      "replace with _xxxx" in {
        val pkg = Codegen.generatorTargetPackage(config("＠"))
        assert(pkg == Seq("_ff20"))
      }
    }

    "packageName contains non BMP characters" should {
      "replace with _xxxx_xxxx" in {
        val pkg = Codegen.generatorTargetPackage(config("""𩸽"""))
        assert(pkg == Seq("_d867_de3d"))
      }
    }
  }

}
