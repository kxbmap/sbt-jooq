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

import sbtjooq.codegen.UnitSpec

class AppendGeneratorTargetDirectoryTest extends UnitSpec {

  "ConfigTransformer.appendGeneratorTargetDirectory" when {

    val appendTo = ConfigTransformer.appendGeneratorTargetDirectory(new java.io.File("foo"))

    "generator target directory is non empty" should {
      "do nothing" in {
        val e =
          <configuration>
            <generator>
              <target>
                <directory>BAR</directory>
              </target>
            </generator>
          </configuration>
        assertXML(e)(appendTo(e))
      }
    }

    "configuration element is empty" should {
      "append generator target directory element" in {
        val x = <configuration/>
        val e =
          <configuration>
            <generator>
              <target>
                <directory>foo</directory>
              </target>
            </generator>
          </configuration>
        assertXML(e)(appendTo(x))
      }
    }

    "generator element is empty" should {
      "append target directory element" in {
        val x =
          <configuration>
            <generator/>
          </configuration>
        val e =
          <configuration>
            <generator>
              <target>
                <directory>foo</directory>
              </target>
            </generator>
          </configuration>
        assertXML(e)(appendTo(x))
      }
    }

    "target element is empty" should {
      "append directory element" in {
        val x =
          <configuration>
            <generator>
              <target/>
            </generator>
          </configuration>
        val e =
          <configuration>
            <generator>
              <target>
                <directory>foo</directory>
              </target>
            </generator>
          </configuration>
        assertXML(e)(appendTo(x))
      }
    }

    "directory element is empty" should {
      "append file name" in {
        val x =
          <configuration>
            <generator>
              <target>
                <directory/>
              </target>
            </generator>
          </configuration>
        val e =
          <configuration>
            <generator>
              <target>
                <directory>foo</directory>
              </target>
            </generator>
          </configuration>
        assertXML(e)(appendTo(x))
      }
    }

    "target element has only packageName element" should {
      "append directory element" in {
        val x =
          <configuration>
            <generator>
              <target>
                <packageName>com.example</packageName>
              </target>
            </generator>
          </configuration>
        val e =
          <configuration>
            <generator>
              <target>
                <packageName>com.example</packageName>
                <directory>foo</directory>
              </target>
            </generator>
          </configuration>
        assertXML(e)(appendTo(x))
      }
    }
  }

}
