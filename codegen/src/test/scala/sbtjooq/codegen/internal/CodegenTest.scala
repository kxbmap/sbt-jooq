package sbtjooq.codegen.internal

import sbtjooq.codegen.UnitSpec

class CodegenTest extends UnitSpec {

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
