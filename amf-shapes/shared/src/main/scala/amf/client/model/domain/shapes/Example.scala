package amf.client.model.domain.shapes

import amf.client.model.domain.{DataNode, DomainElement, Linkable, NamedDomainElement}
import amf.client.model.{Annotations, BoolField, StrField}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}
import amf.plugins.domain.shapes.models.{Example => InternalExample}
import amf.client.convert.shapeconverters.ShapeClientConverters._

/**
  * Example model class
  */
@JSExportAll
case class Example(override private[amf] val _internal: InternalExample)
    extends DomainElement
    with Linkable
    with NamedDomainElement {

  @JSExportTopLevel("model.domain.Example")
  def this() = this(InternalExample())

  def name: StrField = _internal.name

  def displayName: StrField = _internal.displayName

  def description: StrField = _internal.description

  def value: StrField = _internal.raw

  def structuredValue: DataNode = _internal.structuredValue

  def strict: BoolField = _internal.strict

  def mediaType: StrField = _internal.mediaType

  def location: ClientOption[String] = _internal.location().asClient

  def withName(name: String): this.type = {
    _internal.withName(name, new Annotations())
    this
  }

  def withDisplayName(displayName: String): this.type = {
    _internal.withDisplayName(displayName)
    this
  }

  def withDescription(description: String): this.type = {
    _internal.withDescription(description)
    this
  }

  def withValue(value: String): this.type = {
    _internal.withValue(value)
    this
  }

  def withStructuredValue(value: DataNode): this.type = {
    _internal.withStructuredValue(value)
    this
  }

  def withStrict(strict: Boolean): this.type = {
    _internal.withStrict(strict)
    this
  }

  def withMediaType(mediaType: String): this.type = {
    _internal.withMediaType(mediaType)
    this
  }

  override def linkCopy(): Example = _internal.linkCopy()

  def toJson: String = _internal.toJson

  def toYaml: String = _internal.toYaml
}
