package sbtjooq.codegen.internal

import java.util.Properties
import scala.collection.JavaConverters._
import scala.xml.{NodeSeq, Text}

object VariableExpander {

  def apply(expand: PartialFunction[Any, NodeSeq] = default): Any => NodeSeq =
    expand.applyOrElse(_, fallback)

  def default: PartialFunction[Any, NodeSeq] = {
    case props: Properties =>
      props.entrySet().asScala.map { e =>
        <property>
          <key>{e.getKey}</key>
          <value>{e.getValue}</value>
        </property>
      }.toSeq
  }

  def fallback: Any => NodeSeq =
    x => Text(x.toString)

}
