package amf.plugins.document.webapi.parser.spec

import amf.client.remod.amfcore.config.ShapeRenderOptions
import amf.core.emitter.{BaseEmitters, Emitter, EntryEmitter, PartEmitter, SpecOrdering}
import amf.core.errorhandling.ErrorHandler
import amf.core.metamodel.Field
import amf.core.model.document.BaseUnit
import amf.core.model.domain.extensions.{DomainExtension, ShapeExtension}
import amf.core.model.domain.{DomainElement, Linkable, RecursiveShape, Shape}
import amf.core.parser.FieldEntry
import amf.core.remote.Vendor
import amf.plugins.document.webapi.contexts.SpecEmitterContext
import amf.plugins.document.webapi.contexts.emitter.OasLikeSpecEmitterContext
import amf.plugins.document.webapi.contexts.emitter.async.Async20SpecEmitterContext
import amf.plugins.document.webapi.contexts.emitter.jsonschema.JsonSchemaEmitterContext
import amf.plugins.document.webapi.contexts.emitter.oas.Oas3SpecEmitterContext
import amf.plugins.document.webapi.contexts.emitter.raml.RamlSpecEmitterContext
import amf.plugins.document.webapi.parser.OasTypeDefMatcher
import amf.plugins.document.webapi.parser.spec.declaration.{
  CustomFacetsEmitter,
  SchemaVersion,
  ShapeEmitterContext,
  TagToReferenceEmitter
}
import amf.plugins.document.webapi.parser.spec.declaration.emitters.DefinitionsQueue
import amf.plugins.document.webapi.parser.spec.declaration.emitters.annotations.{
  AnnotationEmitter,
  FacetsInstanceEmitter
}
import amf.plugins.document.webapi.parser.spec.oas.emitters.{CompactEmissionContext, OasLikeExampleEmitters}
import amf.plugins.domain.shapes.models.{Example, TypeDef}
import org.yaml.model.{YDocument, YType}

import scala.util.matching.Regex

case class SpecContextShapeAdapter(spec: SpecEmitterContext) extends ShapeEmitterContext {
  override def schemasDeclarationsPath: String = spec match {
    case ctx: OasLikeSpecEmitterContext => ctx.schemasDeclarationsPath
    case _                              => throw new Exception("Spec - Can only be called from OasLike!")
  }

  override def tagToReferenceEmitter: (DomainElement, Seq[BaseUnit]) => TagToReferenceEmitter =
    spec.factory.tagToReferenceEmitter

  override def ref(b: YDocument.PartBuilder, url: String): Unit = spec.ref(b, url)

  override def eh: ErrorHandler = spec.eh

  override def annotationEmitter: (DomainExtension, SpecOrdering) => AnnotationEmitter = spec.factory.annotationEmitter

  override def vendor: Vendor = spec.vendor

  override def isOasLike: Boolean = spec.isInstanceOf[OasLikeSpecEmitterContext]

  override def isRaml: Boolean = spec.isInstanceOf[RamlSpecEmitterContext]

  override def factoryIsOas3: Boolean = spec.isInstanceOf[Oas3SpecEmitterContext]

  override def schemaVersion: SchemaVersion = spec match {
    case ctx: OasLikeSpecEmitterContext => ctx.schemaVersion
    case _                              => throw new Exception("Spec - Can only be called from OasLike!")
  }

  override def matchType(ramlType: String, format: String, default: TypeDef): TypeDef = spec match {
    case ctx: OasLikeSpecEmitterContext => OasTypeDefMatcher.matchType(ramlType, format, default)
    case _                              => throw new Exception("Spec - Can only be called from OasLike!")
  }

  override def matchFormat(typeDef: TypeDef): Option[String] = spec match {
    case ctx: OasLikeSpecEmitterContext => ctx.typeDefMatcher.matchFormat(typeDef)
    case _                              => throw new Exception("Spec - Can only be called from OasLike!")
  }

  override def matchType(typeDef: TypeDef): String = spec match {
    case ctx: OasLikeSpecEmitterContext => ctx.typeDefMatcher.matchType(typeDef)
    case _                              => throw new Exception("Spec - Can only be called from OasLike!")
  }

  override def options: ShapeRenderOptions = spec.options

  override def customFacetsEmitter: (FieldEntry, SpecOrdering, Seq[BaseUnit]) => CustomFacetsEmitter =
    spec.factory.customFacetsEmitter

  override def typeEmitters(shape: Shape,
                            ordering: SpecOrdering,
                            ignored: Seq[Field],
                            references: Seq[BaseUnit],
                            pointer: Seq[String],
                            schemaPath: Seq[(String, String)]): Seq[Emitter] = spec match {
    case ctx: OasLikeSpecEmitterContext =>
      ctx.factory.typeEmitters(shape, ordering, ignored, references, pointer, schemaPath)
    case _ => throw new Exception("Spec - Can only be called from OasLike!")
  }

  override def filterLocal[T <: DomainElement](elements: Seq[T]): Seq[T] = spec.filterLocal(elements)

  override def ramlTypePropertyEmitter(typeName: String, shape: Shape): Option[BaseEmitters.MapEntryEmitter] =
    spec.ramlTypePropertyEmitter(typeName, shape)

  override def arrayEmitter(key: String, f: FieldEntry, ordering: SpecOrdering, valuesTag: YType): EntryEmitter =
    spec.arrayEmitter(key, f, ordering, valuesTag)

  override def localReference(reference: Linkable): PartEmitter = spec.localReference(reference)

  override def definitionsQueue: DefinitionsQueue = spec match {
    case ctx: CompactEmissionContext => ctx.definitionsQueue
    case _                           => throw new Exception("Spec - Can only be called from Compact!")
  }

  override def forceEmission: Option[String] = spec match {
    case ctx: CompactEmissionContext => ctx.forceEmission
    case _                           => throw new Exception("Spec - Can only be called from Compact!")
  }

  override def setForceEmission(value: Option[String]): Unit = spec match {
    case ctx: CompactEmissionContext => ctx.forceEmission = value
    case _                           => throw new Exception("Spec - Can only be called from Compact!")
  }

  override def nameRegex: Regex = spec match {
    case ctx: CompactEmissionContext => ctx.nameRegex
    case _                           => throw new Exception("Spec - Can only be called from Compact!")
  }

  override def isJsonContext: Boolean = spec.isInstanceOf[JsonSchemaEmitterContext]

  override def externalLink(link: Linkable, refs: Seq[BaseUnit]): Option[BaseUnit] = spec.externalLink(link, refs)

  override def externalReference(reference: Linkable): PartEmitter = spec match {
    case ctx: RamlSpecEmitterContext => ctx.externalReference(reference)
    case _                           => throw new Exception("Spec - Can only be called from Raml!")
  }

  override def recursiveShapeEmitter: (RecursiveShape, SpecOrdering, Seq[(String, String)]) => EntryEmitter =
    spec match {
      case ctx: OasLikeSpecEmitterContext => ctx.factory.recursiveShapeEmitter
      case _                              => throw new Exception("Spec - Can only be called from OasLike!")
    }

  override def oasTypePropertyEmitter(typeName: String, shape: Shape): BaseEmitters.MapEntryEmitter =
    spec.oasTypePropertyEmitter(typeName, shape)

  override def exampleEmitter
    : (Boolean, Option[Example], SpecOrdering, Seq[Example], Seq[BaseUnit]) => OasLikeExampleEmitters = spec match {
    case ctx: OasLikeSpecEmitterContext => ctx.factory.exampleEmitter
    case _                              => throw new Exception("Spec - Can only be called from OasLike!")
  }

  override def anyOfKey: String = spec match {
    case ctx: OasLikeSpecEmitterContext => ctx.anyOfKey
    case _                              => throw new Exception("Spec - Can only be called from OasLike!")
  }

  override def localReferenceEntryEmitter(key: String, reference: Linkable): EntryEmitter = spec match {
    case ctx: RamlSpecEmitterContext => ctx.localReferenceEntryEmitter(key, reference)
    case _                           => throw new Exception("Spec - Can only be called from Raml!")
  }

  override def toAsyncNext(schemaVersion: SchemaVersion): ShapeEmitterContext =
    copy(new Async20SpecEmitterContext(spec.eh, schemaVersion = schemaVersion))

  override def toRamlNext: ShapeEmitterContext = copy(spec = toRaml(spec))

  override def toOasNext: ShapeEmitterContext = copy(spec = toOas(spec))

  override def facetsInstanceEmitter: (ShapeExtension, SpecOrdering) => FacetsInstanceEmitter =
    spec.factory.facetsInstanceEmitter
}
