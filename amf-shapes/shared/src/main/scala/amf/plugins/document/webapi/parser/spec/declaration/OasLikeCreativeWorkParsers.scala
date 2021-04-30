package amf.plugins.document.webapi.parser.spec.declaration

import amf.core.parser.{Annotations, _}
import amf.core.remote.{Oas, Raml}
import amf.core.utils.AmfStrings
import amf.plugins.document.webapi.parser.spec.common.{AnnotationParser, QuickFieldParsingOps}
import amf.plugins.document.webapi.parser.spec.oas.parser.types.ShapeParserContext
import amf.plugins.document.webapi.vocabulary.VocabularyMappings
import amf.plugins.domain.shapes.metamodel.CreativeWorkModel
import amf.plugins.domain.webapi.models.CreativeWork
import amf.validations.ShapeParserSideValidations.UnexpectedVendor
import org.yaml.model.{YMap, YNode}

object OasLikeCreativeWorkParser {
  def parse(node: YNode, parentId: String)(implicit ctx: ShapeParserContext): CreativeWork =
    OasLikeCreativeWorkParser(node, parentId).parse()
}

case class OasLikeCreativeWorkParser(node: YNode, parentId: String)(implicit val ctx: ShapeParserContext)
    extends QuickFieldParsingOps {

  def parse(): CreativeWork = {
    val map          = node.as[YMap]
    val creativeWork = CreativeWork(node)

    map.key("url", CreativeWorkModel.Url in creativeWork)
    map.key("description", CreativeWorkModel.Description in creativeWork)
    map.key("title".asOasExtension, CreativeWorkModel.Title in creativeWork)

    creativeWork.adopted(parentId)
    AnnotationParser(creativeWork, map).parse()

    // TODO: Shapes REMOD - uncomment
//    if (ctx.syntax == Oas2Syntax || ctx.syntax == Oas3Syntax)
//      ctx.closedShape(creativeWork.id, map, "externalDoc")
    creativeWork
  }
}

case class RamlCreativeWorkParser(node: YNode)(implicit val ctx: ShapeParserContext) extends QuickFieldParsingOps {

  def parse(): CreativeWork = {

    val map           = node.as[YMap]
    val documentation = CreativeWork(Annotations.valueNode(node))

    map.key("title", (CreativeWorkModel.Title in documentation).allowingAnnotations)
    map.key("content", (CreativeWorkModel.Description in documentation).allowingAnnotations)

    val url = ctx.vendor match {
      case _: Oas  => "url"
      case _: Raml => "url".asRamlAnnotation
      case other =>
        ctx.eh.violation(UnexpectedVendor, s"Unexpected vendor '$other'", node)
        "url"
    }

    map.key(url, CreativeWorkModel.Url in documentation)

    AnnotationParser(documentation, map, List(VocabularyMappings.documentationItem)).parse()

    documentation
  }
}
