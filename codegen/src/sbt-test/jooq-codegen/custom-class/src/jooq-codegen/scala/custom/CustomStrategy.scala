package custom

import org.jooq.codegen.DefaultGeneratorStrategy
import org.jooq.meta.Definition

class CustomStrategy extends DefaultGeneratorStrategy {

  override def getJavaIdentifier(definition: Definition): String =
    s"CUSTOMIZED_${definition.getOutputName}"

}
