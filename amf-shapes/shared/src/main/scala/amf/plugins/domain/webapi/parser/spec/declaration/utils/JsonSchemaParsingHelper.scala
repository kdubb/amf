package amf.plugins.domain.webapi.parser.spec.declaration.utils

import amf.core.model.domain.Shape
import amf.plugins.document.webapi.parser.spec.oas.parser.types.ShapeParserContext
import amf.plugins.domain.shapes.models.UnresolvedShape
import org.yaml.model.{IllegalTypeHandler, YMapEntry}

object JsonSchemaParsingHelper {
  def createTemporaryShape(adopt: Shape => Unit,
                           schemaEntry: YMapEntry,
                           ctx: ShapeParserContext,
                           fullRef: String): UnresolvedShape = {

    implicit val eh: IllegalTypeHandler = ctx.eh

    val tmpShape =
      UnresolvedShape(fullRef, schemaEntry)
        .withName(fullRef)
        .withId(fullRef)
        .withSupportsRecursion(true)
    tmpShape.unresolved(fullRef, schemaEntry, "warning")(ctx)
    tmpShape.withContext(ctx)
    adopt(tmpShape)
    ctx.registerJsonSchema(fullRef, tmpShape)
    tmpShape
  }
}
