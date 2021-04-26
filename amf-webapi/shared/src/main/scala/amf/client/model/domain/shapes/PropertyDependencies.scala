package amf.client.model.domain.shapes

import amf.client.convert.WebApiClientConverters.ClientList
import amf.client.model.StrField
import amf.client.model.domain.DomainElement

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

import amf.plugins.domain.shapes.models.{PropertyDependencies => InternalPropertyDependencies}
import amf.client.convert.shapeconverters.ShapeClientConverters._
/**
  * Property dependencies model class
  */
@JSExportAll
case class PropertyDependencies(override private[amf] val _internal: InternalPropertyDependencies)
    extends DomainElement {

  @JSExportTopLevel("model.domain.PropertyDependencies")
  def this() = this(InternalPropertyDependencies())

  def source: StrField             = _internal.propertySource
  def target: ClientList[StrField] = _internal.propertyTarget.asClient

  def withPropertySource(propertySource: String): this.type = {
    _internal.withPropertySource(propertySource)
    this
  }

  def withPropertyTarget(propertyTarget: ClientList[String]): this.type = {
    _internal.withPropertyTarget(propertyTarget.asInternal)
    this
  }
}
