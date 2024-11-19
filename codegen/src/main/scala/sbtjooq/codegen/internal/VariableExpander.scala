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
import scala.collection.JavaConverters.*
import scala.xml.{Elem, NodeSeq, Text}

object VariableExpander {

  type Handler = PartialFunction[Any, NodeSeq]

  def apply(vars: Map[String, Any], handler: Handler = defaultHandler): VariableExpander =
    vars.get(_).map(handler.applyOrElse(_, fallback))

  private def fallback: Any => NodeSeq =
    x => Text(x.toString)

  def defaultHandler: Handler = {
    case props: Properties =>
      props.entrySet().asScala.toSeq.map(e => prop(e.getKey, e.getValue))
  }

  private def prop(key: Any, value: Any): Elem =
    <property>
      <key>{key}</key>
      <value>{value}</value>
    </property>

}
