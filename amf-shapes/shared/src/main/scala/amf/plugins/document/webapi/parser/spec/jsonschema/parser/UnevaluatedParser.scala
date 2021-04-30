package amf.plugins.document.webapi.parser.spec.jsonschema.parser

import amf.core.annotations.SynthesizedField
import amf.core.metamodel.Field
import amf.core.model.domain.AmfScalar
import amf.core.parser.errorhandler.ParserErrorHandler
import amf.core.parser.{Annotations, YMapOps}
import amf.core.validation.core.ValidationSpecification
import amf.plugins.document.webapi.parser.spec.common.QuickFieldParsingOps
import amf.plugins.document.webapi.parser.spec.declaration.{OasTypeParser, SchemaVersion}
import amf.plugins.document.webapi.parser.spec.oas.parser.types.ShapeParserContext
import amf.plugins.domain.shapes.metamodel.{ArrayShapeModel, NodeShapeModel}
import amf.plugins.domain.shapes.models.AnyShape
import amf.validations.ShapeParserSideValidations.{InvalidUnevaluatedItemsType, InvalidUnevaluatedPropertiesType}
import org.yaml.model.{YMap, YType}

case class UnevaluatedInfo(key: String, booleanField: Field, schemaField: Field, error: ValidationSpecification, message: String)

object UnevaluatedParser {
  val unevaluatedPropertiesInfo: UnevaluatedInfo = UnevaluatedInfo("unevaluatedProperties",
    NodeShapeModel.UnevaluatedProperties,
    NodeShapeModel.UnevaluatedPropertiesSchema,
    InvalidUnevaluatedPropertiesType,
    "Invalid part type for unevaluated properties node. Should be a boolean or a map",
    )

  val unevaluatedItemsInfo: UnevaluatedInfo =
    UnevaluatedInfo("unevaluatedItems", ArrayShapeModel.UnevaluatedItems, ArrayShapeModel.UnevaluatedItemsSchema, InvalidUnevaluatedItemsType, "Invalid part type for unevaluated items node. Should be a boolean or a map")
}

class UnevaluatedParser(version: SchemaVersion, info: UnevaluatedInfo)(implicit ctx: ShapeParserContext) extends QuickFieldParsingOps {

  private val UnevaluatedInfo(key, booleanField, schemaField, error, message) = info

  def parse(map: YMap, shape: AnyShape) {
    map.key(key).foreach { entry =>
      entry.value.tagType match {
        case YType.Bool => (booleanField in shape).explicit(entry)
        case YType.Map =>
          OasTypeParser(entry, s => s.adopted(shape.id), version).parse().foreach { s =>
            shape.set(booleanField, AmfScalar("true"), Annotations(SynthesizedField()))
            shape.set(schemaField, s, Annotations(entry))
          }
        case _ =>
          ctx.eh.violation(error, shape.id, message, entry)
      }
    }
  }
}
