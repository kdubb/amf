package amf.client.model.domain.shapes

import amf.client.convert.WebApiClientConverters.ClientList
import amf.client.model.BoolField
import amf.client.model.domain.Shape

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

import amf.plugins.domain.shapes.models.{TupleShape => InternalTupleShape}
import amf.client.convert.shapeconverters.ShapeClientConverters._

@JSExportAll
case class TupleShape(private[amf] override val _internal: InternalTupleShape) extends DataArrangeShape(_internal) {

  @JSExportTopLevel("model.domain.TupleShape")
  def this() = this(InternalTupleShape())

  def items: ClientList[Shape] = _internal.items.asClient

  def withItems(items: ClientList[Shape]): this.type = {
    _internal.withItems(items.asInternal)
    this
  }

  def closedItems: BoolField = _internal.closedItems

  def withClosedItems(closedItems: Boolean): this.type = {
    _internal.withClosedItems(closedItems)
    this
  }

  def additionalItemsSchema: Shape = _internal.additionalItemsSchema

  override def linkCopy(): TupleShape = _internal.linkCopy()
}
