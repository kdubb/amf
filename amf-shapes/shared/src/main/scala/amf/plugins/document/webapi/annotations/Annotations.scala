package amf.plugins.document.webapi.annotations

import amf.core.model.domain.{AmfElement, Annotation, AnnotationGraphLoader, EternalSerializedAnnotation}

case class ParsedJSONSchema(rawText: String) extends EternalSerializedAnnotation {
  override val name: String  = "parsed-json-schema"
  override val value: String = rawText
}

object ParsedJSONSchema extends AnnotationGraphLoader {
  override def unparse(value: String, objects: Map[String, AmfElement]): Option[Annotation] =
    Some(ParsedJSONSchema(value))
}

case class InlineDefinition() extends Annotation