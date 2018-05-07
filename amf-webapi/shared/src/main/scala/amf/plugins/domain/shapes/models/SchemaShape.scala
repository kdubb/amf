package amf.plugins.domain.shapes.models

import amf.core.model.StrField
import amf.core.parser.{Annotations, Fields}
import amf.plugins.domain.shapes.metamodel.SchemaShapeModel
import amf.plugins.domain.shapes.metamodel.SchemaShapeModel._
import org.yaml.model.YPart
import amf.core.utils.Strings

case class SchemaShape(override val fields: Fields, override val annotations: Annotations)
    extends AnyShape(fields, annotations) {

  def mediaType: StrField = fields.field(MediaType)

  def withRaw(text: String): SchemaShape            = set(Raw, text)
  def withMediaType(mediaType: String): SchemaShape = set(MediaType, mediaType)

  override def linkCopy(): SchemaShape = SchemaShape().withId(id)

  override def meta = SchemaShapeModel

  /** Value , path + field value that is used to compose the id when the object its adopted */
  override def componentId: String = "/schema/" + name.option().getOrElse("default-schema").urlComponentEncoded

}

object SchemaShape {

  def apply(): SchemaShape = apply(Annotations())

  def apply(ast: YPart): SchemaShape = apply(Annotations(ast))

  def apply(annotations: Annotations): SchemaShape = SchemaShape(Fields(), annotations)
}
