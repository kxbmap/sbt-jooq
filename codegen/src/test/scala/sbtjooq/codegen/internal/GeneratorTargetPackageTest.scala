package sbtjooq.codegen.internal

import sbtjooq.codegen.UnitSpec

class GeneratorTargetPackageTest extends UnitSpec {

  "GeneratorTarget.packageName" when {

    def config(name: String) =
      <configuration>
        <generator>
          <target>
            <packageName>{name}</packageName>
          </target>
        </generator>
      </configuration>

    "packageName is blank" should {
      "returns default package" in {
        val pkg = GeneratorTarget.packageName(config("  "))
        assert(pkg == Seq("org", "jooq", "generated"))
      }
    }

    "part of packageName starts with not UnicodeIdentifierStart character" should {
      "prepend underscore to the part" in {
        val pkg = GeneratorTarget.packageName(config("com.example.42"))
        assert(pkg == Seq("com", "example", "_42"))
      }
    }

    "part of packageName contains non UnicodeIdentifierPart but ASCII characters" should {
      "replace chars with _xx" in {
        val pkg = GeneratorTarget.packageName(config("com.example.@"))
        assert(pkg == Seq("com", "example", "_40"))
      }
    }

    "part of packageName contains non UnicodeIdentifierPart and non ASCII characters" should {
      "replace chars with _xxxx" in {
        val pkg = GeneratorTarget.packageName(config("com.example.＠"))
        assert(pkg == Seq("com", "example", "_ff20"))
      }
    }

    "part of packageName contains non BMP characters" should {
      "replace chars with _xxxx_xxxx" in {
        val pkg = GeneratorTarget.packageName(config("""com.example.𩸽"""))
        assert(pkg == Seq("com", "example", "_d867_de3d"))
      }
    }
  }

}
