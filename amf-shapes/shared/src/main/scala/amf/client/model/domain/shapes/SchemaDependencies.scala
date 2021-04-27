package amf.client.model.domain.shapes

import amf.client.model.StrField
import amf.client.model.domain.{DomainElement, Shape}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

import amf.plugins.domain.shapes.models.{SchemaDependencies => InternalSchemaDependencies}
import amf.client.convert.shapeconverters.ShapeClientConverters._

/**
  * Schema dependencies model class
  */
@JSExportAll
case class SchemaDependencies(override private[amf] val _internal: InternalSchemaDependencies)
  extends DomainElement {

  @JSExportTopLevel("model.domain.SchemaDependencies")
  def this() = this(InternalSchemaDependencies())

  def source: StrField = _internal.propertySource

  def target: Shape = _internal.schemaTarget

  def withPropertySource(propertySource: String): this.type = {
    _internal.withPropertySource(propertySource)
    this
  }

  def withSchemaTarget(schema: Shape): this.type = {
    _internal.withSchemaTarget(schema)
    this
  }
}
