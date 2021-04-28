package amf.plugins.document.webapi.contexts.parser.adapters
import amf.core.client.ParsingOptions
import amf.core.model.document.Fragment
import amf.core.model.domain.Shape
import amf.core.model.domain.extensions.CustomDomainProperty
import amf.core.parser.errorhandler.ParserErrorHandler
import amf.core.parser.{
  Annotations,
  Declarations,
  FragmentRef,
  FutureDeclarations,
  ParsedReference,
  ParserContext,
  SearchScope
}
import amf.core.remote.{Syntax, Vendor}
import amf.plugins.document.webapi.contexts.{JsonSchemaRefGuide, WebApiContext}
import amf.plugins.document.webapi.contexts.parser.OasLikeWebApiContext
import amf.plugins.document.webapi.contexts.parser.async.Async20WebApiContext
import amf.plugins.document.webapi.contexts.parser.oas.{JsonSchemaWebApiContext, Oas3WebApiContext}
import amf.plugins.document.webapi.contexts.parser.raml.{
  Raml08WebApiContext,
  Raml10WebApiContext,
  RamlWebApiContext,
  RamlWebApiContextType
}
import amf.plugins.document.webapi.parser.spec.{
  OasLikeWebApiDeclarations,
  OasWebApiDeclarations,
  WebApiDeclarations,
  toOasDeclarations
}
import amf.plugins.document.webapi.parser.spec.common.YMapEntryLike
import amf.plugins.document.webapi.parser.spec.declaration.{JSONSchemaVersion, SchemaVersion}
import amf.plugins.document.webapi.parser.spec.jsonschema.AstIndex
import amf.plugins.document.webapi.parser.spec.oas.parser.types.{
  Async20LocalReferencedDeclaration,
  LocalReferencedDeclaration,
  Oas20LocalReferencedDeclaration,
  Oas30LocalReferencedDeclaration,
  ShapeCloser,
  ShapeParserContext
}
import amf.plugins.domain.shapes.models.{AnyShape, Example}
import amf.plugins.domain.webapi.parser.spec.SpecSyntax
import amf.plugins.domain.webapi.parser.spec.declaration.TypeInfo
import org.yaml.model.{YMap, YNode, YPart}

import scala.collection.mutable

case class WebApiAdapterShapeParserContext(ctx: WebApiContext) extends ShapeParserContext {
  override def rootContextDocument: String = ctx.rootContextDocument

  override def loc: String = ctx.rootContextDocument

  override def eh: ParserErrorHandler = ctx.eh

  override def vendor: Vendor = ctx.vendor

  override def refs: Seq[ParsedReference] = ctx.refs

  override def fragments: Map[String, FragmentRef] = ctx.declarations.fragments

  override def maxYamlReferences: Option[Long] = ctx.options.getMaxYamlReferences

  override def futureDeclarations: FutureDeclarations = ctx.futureDeclarations

  override def refBuilder: LocalReferencedDeclaration = ctx match {
    case _: Oas3WebApiContext    => Oas30LocalReferencedDeclaration
    case _: Async20WebApiContext => Async20LocalReferencedDeclaration
    case _                       => Oas20LocalReferencedDeclaration
  }

  override def closer: ShapeCloser = ShapeCloserAdapter(ctx)

  override def promotedFragments: Seq[Fragment] = ctx.declarations.promotedFragments

  override def computeJsonSchemaVersion(ast: YNode): SchemaVersion = ctx.computeJsonSchemaVersion(ast)

  override def link(node: YNode): Either[String, YNode] = ctx.link(node)

  override def findExample(key: String, scope: SearchScope.Scope): Option[Example] =
    ctx.declarations.findExample(key, scope)

  override def findNamedExample(key: String, error: Option[String => Unit]): Option[Example] =
    ctx.declarations.findNamedExample(key, error)

  override def findNamedExampleOrError(ast: YPart)(key: String): Example =
    ctx.declarations.findNamedExampleOrError(ast)(key)

  override def obtainRemoteYNode(ref: String, refAnnotations: Annotations): Option[YNode] =
    ctx.obtainRemoteYNode(ref, refAnnotations)(ctx)

  override def isMainFileContext: Boolean = ctx match {
    case oasCtx: OasLikeWebApiContext => oasCtx.isMainFileContext
    case _                            => throw new Exception("not valid for something not oas")
  }

  override def registerJsonSchema(ref: String, shape: AnyShape): Unit = ctx.registerJsonSchema(ref, shape)

  override def parseRemoteJSONPath(ref: String): Option[AnyShape] = ctx match {
    case oasCtx: OasLikeWebApiContext => oasCtx.parseRemoteJSONPath(ref)
    case _                            => throw new Exception("not valid for something not oas")
  }

  override def findLocalJSONPath(path: String): Option[YMapEntryLike] = ctx.findLocalJSONPath(path)

  override def findJsonSchema(url: String): Option[AnyShape] = ctx.findJsonSchema(url)

  override def findType(key: String, scope: SearchScope.Scope, error: Option[String => Unit]): Option[AnyShape] =
    ctx.declarations.findType(key, scope, error)

  override def findAnnotation(key: String, scope: SearchScope.Scope): Option[CustomDomainProperty] =
    ctx.declarations.findAnnotation(key, scope)

  override def linkTypes: Boolean = ctx match {
    case _: Raml08WebApiContext => false
    case _                      => true
  }

  override def promoteExternaltoDataTypeFragment(text: String, fullRef: String, shape: Shape): Shape =
    ctx.declarations.promoteExternaltoDataTypeFragment(text, fullRef, shape)

  override def specSyntax: SpecSyntax = ctx.syntax

  override def closedRamlTypeShape(shape: Shape, ast: YMap, shapeType: String, typeInfo: TypeInfo): Unit = ctx match {
    case ramlCtx: RamlWebApiContext => ramlCtx.closedRamlTypeShape(shape, ast, shapeType, typeInfo)
    case _                          => throw new Exception("not valid for something not raml")
  }

  override def libraries: Map[String, Declarations] = ctx.declarations.libraries

  override def nodeRefIds: Map[YNode, String] = ctx.nodeRefIds.toMap

  override def findInExternals(url: String): Option[AnyShape] = ctx match {
    case ramlCtx: RamlWebApiContext => ramlCtx.declarations.findInExternals(url)
    case _                          => throw new Exception("not valid for something not raml")
  }

  override def findInExternalsLibs(lib: String, name: String): Option[AnyShape] = ctx match {
    case ramlCtx: RamlWebApiContext => ramlCtx.declarations.findInExternalsLibs(lib, name)
    case _                          => throw new Exception("not valid for something not raml")
  }

  override def registerExternalRef(external: (String, AnyShape)): Unit = ctx match {
    case ramlCtx: RamlWebApiContext => ramlCtx.declarations.registerExternalRef(external)
    case _                          => throw new Exception("not valid for something not raml")
  }

  override def getInheritedDeclarations = {
    ctx match {
      case _: Raml08WebApiContext => Some(ctx.declarations)
      case _                      => None
    }
  }

  override def defaultSchemaVersion: JSONSchemaVersion = ctx.defaultSchemaVersion

  override def setJsonSchemaAST(value: YNode): Unit = ctx.setJsonSchemaAST(value)

  override def localJSONSchemaContext: Option[YNode] = ctx.localJSONSchemaContext

  override def setLocalJSONSchemaContext(node: YNode): Unit = ctx.localJSONSchemaContext = Some(node)

  override def discardLocalJSONSchemaContext(): Unit = ctx.localJSONSchemaContext = None

  override def registerExternalLib(path: String, toMap: Map[String, AnyShape]): Unit = ctx match {
    case ramlCtx: RamlWebApiContext => ramlCtx.declarations.registerExternalLib(path, toMap)
    case _                          => throw new Exception("not valid for something not raml")
  }

  override def globalSpace: mutable.Map[String, Any] = ctx.globalSpace

  override def addPromotedFragments(fragments: Seq[Fragment]): Unit = ctx.declarations.promotedFragments ++= fragments

  override def jsonSchemaRefGuide: JsonSchemaRefGuide = ctx.jsonSchemaRefGuide

  override def validateRefFormatWithError(ref: String): Boolean = ctx.validateRefFormatWithError(ref)

  override def copyForJsonSchema(loc: String,
                                 refs: Seq[ParsedReference],
                                 wrapped: ParserContext,
                                 ds: Option[Declarations],
                                 options: ParsingOptions,
                                 defaultSchemaVersion: JSONSchemaVersion,
                                 indexCache: mutable.Map[String, AstIndex]): ShapeParserContext = {
    val ctx = new JsonSchemaWebApiContext(loc,
                                          refs,
                                          wrapped,
                                          ds.map(_.asInstanceOf[WebApiDeclarations]).map(toOasDeclarations),
                                          options,
                                          defaultSchemaVersion)
    ctx.indexCache = indexCache
    WebApiAdapterShapeParserContext(ctx)
  }

  override def indexCache: mutable.Map[String, AstIndex] = ctx.indexCache

  override def supportsVariables: Boolean = ctx match {
    case ramlCtx: RamlWebApiContext => ramlCtx.contextType != RamlWebApiContextType.DEFAULT
    case _                          => throw new Exception("not valid for something not raml")
  }
}

case class ShapeCloserAdapter(ctx: WebApiContext) extends ShapeCloser {
  override def closedShape(node: String, ast: YMap, shape: String): Unit = ctx.closedShape(node, ast, shape)
}
