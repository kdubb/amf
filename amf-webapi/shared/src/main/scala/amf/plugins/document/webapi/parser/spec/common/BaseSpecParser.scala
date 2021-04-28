package amf.plugins.document.webapi.parser.spec.common

import amf.core.metamodel.document.DocumentModel
import amf.core.model.document.DeclaresModel
import amf.core.model.domain.{ArrayNode => _, ScalarNode => _, _}
import amf.core.parser._
import amf.plugins.document.webapi.annotations.{DeclarationKey, DeclarationKeys}
import amf.plugins.document.webapi.contexts.WebApiContext
import amf.validations.ParserSideValidations.PathTemplateUnbalancedParameters
import org.yaml.model._

trait WebApiBaseSpecParser extends BaseSpecParser with SpecParserOps with DeclarationKeyCollector

trait DeclarationKeyCollector {

  private var declarationKeys: List[DeclarationKey] = List.empty

  def addDeclarationKey(key: DeclarationKey): Unit = {
    declarationKeys = key :: declarationKeys
  }

  protected def addDeclarationsToModel(model: DeclaresModel)(implicit ctx: WebApiContext): Unit = {

    val ann        = Annotations(DeclarationKeys(declarationKeys)) ++= Annotations.virtual()
    val declarable = ctx.declarations.declarables()

    // check declaration key to use as source maps for field and value
    if (declarable.nonEmpty || declarationKeys.nonEmpty)
      model.setWithoutId(DocumentModel.Declares, AmfArray(declarable, Annotations.virtual()), ann)

  }
}

trait SpecParserOps extends QuickFieldParsingOps {
  protected def checkBalancedParams(path: String,
                                    value: YNode,
                                    node: String,
                                    property: String,
                                    ctx: WebApiContext): Unit = {
    val pattern1 = "\\{[^}]*\\{".r
    val pattern2 = "}[^{]*}".r
    if (pattern1.findFirstMatchIn(path).nonEmpty || pattern2.findFirstMatchIn(path).nonEmpty) {
      ctx.eh.violation(
        PathTemplateUnbalancedParameters,
        node,
        Some(property),
        "Invalid path template syntax",
        value
      )
    }
  }

}