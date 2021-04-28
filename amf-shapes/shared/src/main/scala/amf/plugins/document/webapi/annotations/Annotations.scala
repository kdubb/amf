package amf.plugins.document.webapi.annotations

import amf.core.model.domain.{
  AmfElement,
  Annotation,
  AnnotationGraphLoader,
  EternalSerializedAnnotation,
  PerpetualAnnotation,
  SerializableAnnotation
}
import org.yaml.model.YMapEntry

case class ParsedJSONSchema(rawText: String) extends EternalSerializedAnnotation {
  override val name: String  = "parsed-json-schema"
  override val value: String = rawText
}

object ParsedJSONSchema extends AnnotationGraphLoader {
  override def unparse(value: String, objects: Map[String, AmfElement]): Option[Annotation] =
    Some(ParsedJSONSchema(value))
}

case class JSONSchemaId(id: String) extends SerializableAnnotation with PerpetualAnnotation {
  override val name: String  = "json-schema-id"
  override val value: String = id
}

object JSONSchemaId extends AnnotationGraphLoader {
  override def unparse(value: String, objects: Map[String, AmfElement]): Option[Annotation] = Some(JSONSchemaId(value))
}

case class InlineDefinition() extends Annotation

case class CollectionFormatFromItems() extends Annotation

case class ParsedJSONExample(rawText: String) extends SerializableAnnotation with PerpetualAnnotation {
  override val name: String  = "parsed-json-example"
  override val value: String = rawText
}

object ParsedJSONExample extends AnnotationGraphLoader {
  override def unparse(value: String, objects: Map[String, AmfElement]): Option[Annotation] =
    Some(ParsedJSONExample(value))
}

// used internally for emission of links that have been inlined. This annotation is removed in resolution
case class ExternalReferenceUrl(url: String) extends Annotation

case class ExternalJsonSchemaShape(original: YMapEntry) extends Annotation

case class SchemaIsJsonSchema() extends Annotation

case class ExternalSchemaWrapper() extends Annotation
