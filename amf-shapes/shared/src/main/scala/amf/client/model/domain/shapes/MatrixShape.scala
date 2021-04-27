package amf.client.model.domain.shapes

import amf.client.model.domain.Shape
import amf.plugins.domain.shapes.models.{ArrayShape => InternalArrayShape}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
class MatrixShape(override private[amf] val _internal: InternalArrayShape) extends ArrayShape(_internal) {

  @JSExportTopLevel("model.domain.MatrixShape")
  def this() = this(InternalArrayShape())

  override def withItems(items: Shape): this.type = {
    items match {
      case array: ArrayShape => super.withItems(items)
      case _                 => throw new Exception("Matrix shapes can only accept arrays as items")
    }
  }
}
