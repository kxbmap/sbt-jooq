/*
 * Copyright 2015 Tsukasa Kitachi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package sbtjooq.codegen.internal

import java.util.Properties
import sbtjooq.codegen.UnitSpec

class ReplaceConfigVariablesTest extends UnitSpec {

  "ConfigTransformer.replaceConfigVariables" when {

    val vars = Map[String, Any](
      "STRING" -> "foo",
      "FILE" -> new java.io.File("xxx.txt"),
      "PROPS" -> {
        val props = new Properties()
        props.setProperty("key1", "value1")
        props.setProperty("key2", "value2")
        props
      },
    )
    val transform = ConfigTransformer.replaceConfigVariables(vars, VariableExpander())

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

    "configuration contains placeholder that refer Properties variable" should {
      "replace with property elements that contains key/value" in {
        val x =
          <configuration>
            <properties>${{PROPS}}</properties>
          </configuration>
        val e =
          <configuration>
            <properties>
              <property>
                <key>key1</key>
                <value>value1</value>
              </property>
              <property>
                <key>key2</key>
                <value>value2</value>
              </property>
            </properties>
          </configuration>
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
