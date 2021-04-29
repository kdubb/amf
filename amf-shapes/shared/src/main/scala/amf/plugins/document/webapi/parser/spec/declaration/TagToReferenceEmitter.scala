package amf.plugins.document.webapi.parser.spec.declaration

import amf.client.remod.amfcore.config.ShapeRenderOptions
import amf.core.annotations.{DeclaredElement, ExternalFragmentRef}
import amf.core.emitter.BaseEmitters.{EntryPartEmitter, MapEntryEmitter, pos, raw}
import amf.core.emitter.{Emitter, EntryEmitter, PartEmitter, SpecOrdering}
import amf.core.errorhandling.ErrorHandler
import amf.core.metamodel.Field
import amf.core.model.document.{BaseUnit, Fragment}
import amf.core.model.domain.extensions.{DomainExtension, ShapeExtension}
import amf.core.model.domain.{DomainElement, Linkable, RecursiveShape, Shape}
import amf.core.parser.{FieldEntry, Position}
import amf.core.remote.Vendor
import amf.plugins.document.webapi.parser.spec.declaration.emitters.DefinitionsQueue
import amf.plugins.document.webapi.parser.spec.declaration.emitters.annotations.{
  AnnotationEmitter,
  FacetsInstanceEmitter
}
import amf.plugins.document.webapi.parser.spec.oas.emitters.OasLikeExampleEmitters
import amf.plugins.domain.shapes.models.{Example, TypeDef}
import amf.plugins.domain.shapes.models.TypeDef.ObjectType
import amf.plugins.domain.webapi.parser.spec.OasShapeDefinitions.appendSchemasPrefix
import org.yaml.model.YDocument.PartBuilder
import org.yaml.model.YType

import scala.util.matching.Regex

trait ShapeEmitterContext {
  def schemasDeclarationsPath: String
  def tagToReferenceEmitter: (DomainElement, Seq[BaseUnit]) => TagToReferenceEmitter
  def ref(b: PartBuilder, url: String): Unit
  def eh: ErrorHandler
  def annotationEmitter: (DomainExtension, SpecOrdering) => AnnotationEmitter
  def vendor: Vendor
  def isOasLike: Boolean
  def isRaml: Boolean
  def factoryIsOas3: Boolean
  def schemaVersion: SchemaVersion
  def matchType(ramlType: String, format: String = "", default: TypeDef = ObjectType): TypeDef
  def matchFormat(typeDef: TypeDef): Option[String]
  def matchType(typeDef: TypeDef): String
  def options: ShapeRenderOptions
  def customFacetsEmitter: (FieldEntry, SpecOrdering, Seq[BaseUnit]) => CustomFacetsEmitter
  def typeEmitters(shape: Shape,
                   ordering: SpecOrdering,
                   ignored: Seq[Field],
                   references: Seq[BaseUnit],
                   pointer: Seq[String],
                   schemaPath: Seq[(String, String)]): Seq[Emitter]

  def filterLocal[T <: DomainElement](elements: Seq[T]): Seq[T]
  def ramlTypePropertyEmitter(typeName: String, shape: Shape): Option[MapEntryEmitter]
  def arrayEmitter(key: String, f: FieldEntry, ordering: SpecOrdering, valuesTag: YType = YType.Str): EntryEmitter
  def toOasNext: ShapeEmitterContext
  def localReference(reference: Linkable): PartEmitter
  def definitionsQueue: DefinitionsQueue
  def forceEmission: Option[String]
  def setForceEmission(value: Option[String])
  def nameRegex: Regex
  def isJsonContext: Boolean
  def externalLink(link: Linkable, refs: Seq[BaseUnit]): Option[BaseUnit]
  def externalReference(reference: Linkable): PartEmitter
  def recursiveShapeEmitter: (RecursiveShape, SpecOrdering, Seq[(String, String)]) => EntryEmitter
  def oasTypePropertyEmitter(typeName: String, shape: Shape): MapEntryEmitter
  def exampleEmitter: (Boolean, Option[Example], SpecOrdering, Seq[Example], Seq[BaseUnit]) => OasLikeExampleEmitters
  def anyOfKey: String
  def localReferenceEntryEmitter(key: String, reference: Linkable): EntryEmitter
  def toAsyncNext(schemaVersion: SchemaVersion): ShapeEmitterContext
  def toRamlNext: ShapeEmitterContext
  def facetsInstanceEmitter: (ShapeExtension, SpecOrdering) => FacetsInstanceEmitter

}

trait TagToReferenceEmitter extends PartEmitter {
  val link: DomainElement

  val label: Option[String] = link match {
    case l: Linkable => l.linkLabel.option()
    case _           => None
  }

  val referenceLabel: String = label.getOrElse(link.id)
}

trait ShapeReferenceEmitter extends TagToReferenceEmitter {

  protected val shapeSpec: ShapeEmitterContext

  def emit(b: PartBuilder): Unit = {
    val lastElementInLinkChain = follow()
    val urlToEmit =
      if (isDeclaredElement(lastElementInLinkChain)) getRefUrlFor(lastElementInLinkChain) else referenceLabel
    shapeSpec.ref(b, urlToEmit)
  }

  protected def getRefUrlFor(element: DomainElement, default: String = referenceLabel) = element match {
    case _: Shape => appendSchemasPrefix(referenceLabel, Some(shapeSpec.vendor))
    case _        => default
  }

  private def isDeclaredElement(element: DomainElement) = element.annotations.contains(classOf[DeclaredElement])

  /** Follow links until first declaration or last element in chain */
  private def follow(): DomainElement = follow(link)

  @scala.annotation.tailrec
  private def follow(element: DomainElement, seenLinks: Seq[String] = Seq()): DomainElement = {
    element match {
      case s: Linkable if s.isLink =>
        s.linkTarget match {
          case Some(t: Linkable) if t.isLink & !t.annotations.contains(classOf[DeclaredElement]) =>
            // If find link which is not a declaration (declarations can be links as well) follow link
            follow(t.linkTarget.get, seenLinks :+ element.id)
          case Some(t) => t
          case None    => s // This is unreachable
        }
      case other => other
    }
  }
}

case class OasShapeReferenceEmitter(link: DomainElement)(implicit val shapeSpec: ShapeEmitterContext)
    extends ShapeReferenceEmitter {

  override def position(): Position = pos(link.annotations)
}

object ReferenceEmitterHelper {

  def emitLinkOr(l: DomainElement with Linkable, b: PartBuilder, refs: Seq[BaseUnit] = Nil)(fallback: => Unit)(
      implicit spec: ShapeEmitterContext): Unit = {
    if (l.isLink)
      spec.tagToReferenceEmitter(l, refs).emit(b)
    else
      fallback
  }
}

case class RamlTagToReferenceEmitter(link: DomainElement, references: Seq[BaseUnit])(
    implicit val spec: ShapeEmitterContext)
    extends PartEmitter
    with TagToReferenceEmitter {

  override def emit(b: PartBuilder): Unit = {
    if (containsRefAnnotation)
      link.annotations.find(classOf[ExternalFragmentRef]).foreach { a =>
        spec.ref(b, a.fragment) // emits with !include
      } else if (linkReferencesFragment)
      spec.ref(b, referenceLabel) // emits with !include
    else
      raw(b, referenceLabel)
  }

  private def containsRefAnnotation = link.annotations.contains(classOf[ExternalFragmentRef])

  private def linkReferencesFragment: Boolean = {
    link match {
      case l: Linkable =>
        l.linkTarget.exists { target =>
          references.exists {
            case f: Fragment => f.encodes == target
            case _           => false
          }
        }
      case _ => false
    }
  }

  override def position(): Position = pos(link.annotations)
}

class RamlLocalReferenceEntryEmitter(override val key: String, reference: Linkable)
    extends EntryPartEmitter(key, RamlLocalReferenceEmitter(reference))

case class RamlLocalReferenceEmitter(reference: Linkable) extends PartEmitter {
  override def emit(b: PartBuilder): Unit = reference.linkLabel.option() match {
    case Some(label) => raw(b, label)
    case None        => throw new Exception("Missing link label")
  }

  override def position(): Position = pos(reference.annotations)
}

case class RamlIncludeReferenceEmitter(reference: Linkable, location: String) extends PartEmitter {

  override def emit(b: PartBuilder): Unit =
    raw(b, s"!include ${location}", YType.Include)

  override def position(): Position = pos(reference.annotations)
}
