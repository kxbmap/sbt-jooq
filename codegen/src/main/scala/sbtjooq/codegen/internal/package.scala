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

package sbtjooq.codegen

import java.io.FileNotFoundException
import sbt._

package object internal {

  implicit class JavaVersionCompanionOps(companion: JavaVersion.type) {
    def get(javaHome: Option[File]): JavaVersion =
      javaHome.fold(systemDefault)(parse(_).fold(sys.error, identity))

    def systemDefault: JavaVersion =
      companion(sys.props("java.version"))

    def parse(javaHome: File): Either[String, JavaVersion] = {
      val releaseFile = javaHome / "release"
      val versionLine = """JAVA_VERSION="(.+)"""".r
      try {
        IO.readLines(releaseFile)
          .collectFirst {
            case versionLine(v) => companion(v)
          }
          .toRight(s"No JAVA_VERSION line in $releaseFile")
      } catch {
        case e: FileNotFoundException => Left(e.getMessage)
      }
    }
  }

  implicit class JavaVersionOps(javaVersion: JavaVersion) {
    def major: Long = javaVersion.numbers match {
      case Vector(1L, x, _*) => x
      case Vector(x, _*) => x
    }

    def isJigsawEnabled: Boolean = major >= 9

    def isJavaEEModulesBundled: Boolean = major <= 10
  }

}
