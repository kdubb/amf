package amf.plugins.domain.payload

import amf.compiler.{AbstractReferenceCollector, ParsedDocument}
import amf.spec.ParserContext
import amf.validation.Validation
import org.yaml.model.YDocument

class PayloadReferenceCollector extends AbstractReferenceCollector {
  override def traverse(document: ParsedDocument, validation: Validation, ctx: ParserContext) = Nil
}
