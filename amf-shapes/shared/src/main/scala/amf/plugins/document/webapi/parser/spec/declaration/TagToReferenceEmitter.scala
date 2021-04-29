package amf.plugins.document.webapi.parser.spec.declaration

import amf.core.annotations.ExternalFragmentRef
import amf.core.emitter.BaseEmitters.{EntryPartEmitter, pos, raw}
import amf.core.emitter.{PartEmitter, SpecOrdering}
import amf.core.errorhandling.ErrorHandler
import amf.core.model.document.{BaseUnit, Fragment}
import amf.core.model.domain.extensions.DomainExtension
import amf.core.model.domain.{DomainElement, Linkable}
import amf.core.parser.Position
import amf.core.remote.Vendor
import amf.plugins.document.webapi.parser.spec.declaration.emitters.annotations.AnnotationEmitter
import amf.plugins.domain.shapes.models.TypeDef
import amf.plugins.domain.shapes.models.TypeDef.ObjectType
import org.yaml.model.YDocument.PartBuilder
import org.yaml.model.YType

trait ShapeEmitterContext {
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
}

trait TagToReferenceEmitter extends PartEmitter {
  val link: DomainElement

  val label: Option[String] = link match {
    case l: Linkable => l.linkLabel.option()
    case _           => None
  }

  val referenceLabel: String = label.getOrElse(link.id)
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
