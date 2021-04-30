package amf.plugins.document.webapi.parser.spec.oas.parser.types

import amf.core.client.ParsingOptions
import amf.core.model.document.Fragment
import amf.core.model.domain.extensions.CustomDomainProperty
import amf.core.model.domain.{FutureDeclarationComponents, Shape}
import amf.core.parser.errorhandler.ParserErrorHandler
import amf.core.parser.{
  Annotations,
  Declarations,
  FragmentRef,
  FutureDeclarations,
  ParsedReference,
  ParserContext,
  ParserErrorHandling,
  SearchScope
}
import amf.core.remote.Vendor
import amf.core.validation.core.ValidationSpecification
import amf.plugins.document.webapi.contexts.JsonSchemaRefGuide
import amf.plugins.document.webapi.parser.spec.common.{YMapEntryLike}
import amf.plugins.document.webapi.parser.spec.declaration.{JSONSchemaVersion, SchemaVersion}
import amf.plugins.document.webapi.parser.spec.jsonschema.AstIndex
import amf.plugins.domain.shapes.models.{AnyShape, Example}
import amf.plugins.domain.webapi.parser.spec.SpecSyntax
import amf.plugins.domain.webapi.parser.spec.declaration.TypeInfo
import org.yaml.model.{YMap, YNode, YPart}

import scala.collection.mutable

abstract class ShapeParserContext(eh: ParserErrorHandler)
    extends ParserErrorHandling(eh)
    with FutureDeclarationComponents {

  def rootContextDocument: String
  def loc: String
  def vendor: Vendor
  def refs: Seq[ParsedReference]
  def fragments: Map[String, FragmentRef]
  def maxYamlReferences: Option[Long]
  def shapes: Map[String, Shape]
  def futureDeclarations: FutureDeclarations
  def refBuilder: LocalReferencedDeclaration
  def closer: ShapeCloser
  def supportsVariables: Boolean

  def promotedFragments: Seq[Fragment]

  def computeJsonSchemaVersion(ast: YNode): SchemaVersion

  def link(node: YNode): Either[String, YNode]

  def findExample(key: String, scope: SearchScope.Scope): Option[Example]

  def findNamedExample(key: String, error: Option[String => Unit] = None): Option[Example]

  def findNamedExampleOrError(ast: YPart)(key: String): Example

  def obtainRemoteYNode(ref: String, refAnnotations: Annotations = Annotations()): Option[YNode]

  def closedShape(node: String, ast: YMap, shape: String): Unit = closer.closedShape(node, ast, shape)

  def isMainFileContext: Boolean

  def registerJsonSchema(ref: String, shape: AnyShape)

  def parseRemoteJSONPath(ref: String): Option[AnyShape]

  def findLocalJSONPath(path: String): Option[YMapEntryLike]

  def findJsonSchema(url: String): Option[AnyShape]

  def findType(key: String, scope: SearchScope.Scope, error: Option[String => Unit] = None): Option[AnyShape]

  def violation(violationId: ValidationSpecification, node: String, message: String): Unit =
    eh.violation(violationId, node, message, rootContextDocument)

  def findAnnotation(key: String, scope: SearchScope.Scope): Option[CustomDomainProperty]

  def linkTypes: Boolean

  def promoteExternaltoDataTypeFragment(text: String, fullRef: String, shape: Shape): Shape

  def specSyntax: SpecSyntax

  def closedRamlTypeShape(shape: Shape, ast: YMap, shapeType: String, typeInfo: TypeInfo): Unit

  def libraries: Map[String, Declarations]

  def nodeRefIds: Map[YNode, String]

  def findInExternals(url: String): Option[AnyShape]

  def findInExternalsLibs(lib: String, name: String): Option[AnyShape]

  def registerExternalRef(external: (String, AnyShape))

  def setJsonSchemaAST(value: YNode): Unit

  def localJSONSchemaContext: Option[YNode]

  def setLocalJSONSchemaContext(node: YNode): Unit

  def discardLocalJSONSchemaContext(): Unit

  def registerExternalLib(path: String, toMap: Map[String, AnyShape]): Unit

  def globalSpace: mutable.Map[String, Any]

  def getInheritedDeclarations: Option[Declarations]

  def defaultSchemaVersion: JSONSchemaVersion

  def addPromotedFragments(fragments: Seq[Fragment]): Unit

  def jsonSchemaRefGuide: JsonSchemaRefGuide

  def validateRefFormatWithError(ref: String): Boolean

  def copyForJsonSchema(loc: String,
                        refs: Seq[ParsedReference],
                        wrapped: ParserContext,
                        ds: Option[Declarations],
                        options: ParsingOptions = ParsingOptions(),
                        defaultSchemaVersion: JSONSchemaVersion,
                        indexCache: mutable.Map[String, AstIndex]): ShapeParserContext

  def indexCache: mutable.Map[String, AstIndex]

  def toOasNext: ShapeParserContext
  def toRamlNext: ShapeParserContext

  def toJsonSchema: ShapeParserContext
  def toJsonSchema(root: String, refs: Seq[ParsedReference]): ShapeParserContext
}
