package amf.plugins.document.webapi.parser.spec.jsonschema

import amf.core.Root
import amf.core.client.ParsingOptions
import amf.core.exception.UnsupportedParsedDocumentException
import amf.core.metamodel.domain.ExternalSourceElementModel
import amf.core.model.document.{EncodesModel, Fragment}
import amf.core.parser.errorhandler.ParserErrorHandler
import amf.core.parser.{Annotations, EmptyFutureDeclarations, ParserContext, SyamlParsedDocument}
import amf.plugins.document.webapi.parser.spec.common.YMapEntryLike
import amf.plugins.document.webapi.parser.spec.declaration.{JSONSchemaVersion, OasTypeParser}
import amf.plugins.document.webapi.parser.spec.jsonschema.AstFinder.getPointedAstOrNode
import amf.plugins.document.webapi.parser.spec.jsonschema.JsonSchemaRootCreator.createRootFrom
import amf.plugins.document.webapi.parser.spec.oas.parser.types.ShapeParserContext
import amf.plugins.domain.shapes.metamodel.SchemaShapeModel
import amf.plugins.domain.shapes.models.{AnyShape, SchemaShape}
import amf.validations.ShapeParserSideValidations.UnableToParseJsonSchema

class JsonSchemaParser {

  def parse(inputFragment: Fragment, pointer: Option[String])(implicit ctx: ShapeParserContext): Option[AnyShape] = {

    val doc: Root     = createRootFrom(inputFragment, pointer, ctx.eh)
    val parsingResult = parse(doc, ctx, new ParsingOptions())

    parsingResult match {
      case encoded: EncodesModel if encoded.encodes.isInstanceOf[AnyShape] =>
        Some(encoded.encodes.asInstanceOf[AnyShape])
      case _ => None
    }
  }

  def parse(document: Root,
            parentContext: ShapeParserContext,
            options: ParsingOptions,
            optionalVersion: Option[JSONSchemaVersion] = None): AnyShape = {

    document.parsed match {
      case parsedDoc: SyamlParsedDocument =>
        val shapeId: String                  = deriveShapeIdFrom(document)
        val JsonReference(url, hashFragment) = JsonReference.buildReference(document.location)
        val jsonSchemaContext                = makeJsonSchemaContext(document, parentContext, url, options)
        val rootAst                          = getPointedAstOrNode(parsedDoc.document.node, shapeId, hashFragment, url)(jsonSchemaContext)
        val version                          = optionalVersion.getOrElse(jsonSchemaContext.computeJsonSchemaVersion(parsedDoc.document.node))
        val parsed =
          OasTypeParser(rootAst,
                        keyValueOrDefault(rootAst)(jsonSchemaContext.eh),
                        shape => shape.withId(shapeId),
                        version = version)(jsonSchemaContext)
            .parse() match {
            case Some(shape) => shape
            case None =>
              throwUnparsableJsonSchemaError(document, shapeId, jsonSchemaContext, rootAst)
              SchemaShape()
                .withId(shapeId)
                .set(ExternalSourceElementModel.Raw, document.raw, Annotations.synthesized())
                .set(SchemaShapeModel.MediaType, "application/json", Annotations.synthesized())
          }
        parsed
      case _ => throw UnsupportedParsedDocumentException
    }
  }

  private def keyValueOrDefault(rootAst: YMapEntryLike)(implicit errorHandler: ParserErrorHandler) = {
    rootAst.key.map(_.as[String]).getOrElse("schema")
  }

  private def makeJsonSchemaContext(document: Root,
                                    parentContext: ShapeParserContext,
                                    url: String,
                                    options: ParsingOptions): ShapeParserContext = {

    val cleanNested = ParserContext(url, document.references, EmptyFutureDeclarations(), parentContext.eh)
    cleanNested.globalSpace = parentContext.globalSpace

    // Apparently, in a RAML 0.8 API spec the JSON Schema has a closure over the schemas declared in the spec...
    val inheritedDeclarations = parentContext.getInheritedDeclarations

    val schemaContext = parentContext.copyForJsonSchema(url,
                                                        document.references,
                                                        cleanNested,
                                                        inheritedDeclarations,
                                                        options,
                                                        parentContext.defaultSchemaVersion,
                                                        parentContext.indexCache)
    schemaContext
  }

  private def deriveShapeIdFrom(doc: Root): String =
    if (doc.location.contains("#")) doc.location else doc.location + "#/"

  private def throwUnparsableJsonSchemaError(document: Root,
                                             shapeId: String,
                                             jsonSchemaContext: ShapeParserContext,
                                             rootAst: YMapEntryLike): Unit = {
    jsonSchemaContext.eh.violation(UnableToParseJsonSchema,
                                   shapeId,
                                   s"Cannot parse JSON Schema at ${document.location}",
                                   rootAst.value)
  }
}
