package amf.plugins.document.webapi.parser.spec.domain

import amf.core.annotations.{ExplicitField, SynthesizedField}
import amf.core.metamodel.domain.extensions.PropertyShapeModel
import amf.core.model.domain.{AmfScalar, Shape}
import amf.core.parser.{Annotations, ScalarNode, YMapOps}
import amf.plugins.document.webapi.contexts.parser.raml.RamlWebApiContext
import amf.plugins.document.webapi.parser.spec.common.{AnnotationParser, YMapEntryLike}
import amf.plugins.document.webapi.parser.spec.declaration._
import amf.plugins.domain.shapes.metamodel.NodeShapeModel
import amf.plugins.domain.shapes.models.ExampleTracking.tracking
import amf.plugins.domain.shapes.models.{AnyShape, NodeShape}
import amf.plugins.domain.webapi.metamodel.PayloadModel
import amf.plugins.domain.webapi.models.Payload
import amf.validations.ParserSideValidations.InvalidPayload
import org.yaml.model._

/**
  *
  */
case class Raml10PayloadParser(entry: YMapEntry, producer: Option[String] => Payload, parseOptional: Boolean = false)(
    implicit ctx: RamlWebApiContext)
    extends RamlPayloadParser(entry: YMapEntry, producer: Option[String] => Payload, parseOptional) {

  override def parse(): Payload = {
    val payload = super.parse()

    entry.value.tagType match {
      case YType.Map => // ignore, in this case it will be parsed in the shape
      case _ =>
        entry.value.to[YMap] match {
          case Right(map) => AnnotationParser(payload, map).parse()
          case _          =>
        }
    }

    entry.value.tagType match {
      case YType.Null =>
        Raml10TypeParser(entry, shape => shape.withName("schema").adopted(payload.id), TypeInfo(), AnyDefaultType)(ctx)
          .parse()
          .foreach { schema =>
            schema.annotations += SynthesizedField()
            ctx.autoGeneratedAnnotation(schema)
            payload.withSchema(tracking(schema, payload.id))
          }
      case _ =>
        Raml10TypeParser(entry, shape => shape.withName("schema").adopted(payload.id), TypeInfo(), AnyDefaultType)(ctx)
          .parse()
          .foreach(s => {
            ctx.autoGeneratedAnnotation(s)
            payload.withSchema(tracking(s, payload.id))
          })

    }

    payload
  }
}

case class Raml08PayloadParser(entry: YMapEntry, producer: Option[String] => Payload, parseOptional: Boolean = false)(
    implicit ctx: RamlWebApiContext)
    extends RamlPayloadParser(entry: YMapEntry, producer: Option[String] => Payload, parseOptional) {

  override def parse(): Payload = {
    val payload = super.parse()

    val mediaType = payload.mediaType.value()

    if (mediaType.endsWith("?")) {
      payload.set(PayloadModel.Optional, value = true)
      payload.set(PayloadModel.MediaType, mediaType.stripSuffix("?"))
    }

    entry.value.tagType match {
      case YType.Null =>
        val shape: AnyShape = AnyShape(entry)
        val anyShape        = shape.withName("schema").adopted(payload.id)
        anyShape.annotations += SynthesizedField()
        payload.withSchema(anyShape)

      case YType.Map =>
        if (List("application/x-www-form-urlencoded", "multipart/form-data").contains(mediaType)) {
          Raml08WebFormParser(entry.value.as[YMap], payload.id).parse().foreach(payload.withSchema)
        } else {
          Raml08TypeParser(entry, (shape: Shape) => shape.adopted(payload.id), isAnnotation = false, AnyDefaultType)
            .parse()
            .foreach(s => payload.withSchema(tracking(s, payload.id)))
        }

      case _ =>
        ctx.violation(
          InvalidPayload,
          payload.id,
          "Invalid payload. Payload must be a map or null"
        )
    }

    payload
  }

}

case class Raml08WebFormParser(map: YMap, parentId: String)(implicit ctx: RamlWebApiContext) {
  def parse(): Option[NodeShape] = {
    map
      .key("formParameters")
      .flatMap(entry => {
        val entries = entry.value.as[YMap].entries
        entries.headOption.map {
          _ =>
            val nodeShape: NodeShape = NodeShape(entry.value)
            val webFormShape         = nodeShape.withName("schema").adopted(parentId)
            entries.foreach(e => {

              Raml08TypeParser(e,
                               (shape: Shape) => shape.adopted(webFormShape.id),
                               isAnnotation = false,
                               StringDefaultType)
                .parse()
                .foreach {
                  s =>
                    val property = webFormShape.withProperty(e.key.toString())
                    // by default 0.8 fields are optional
                    property.withMinCount(0)
                    // find for an explicit annotation
                    e.value.asOption[YMap] match {
                      case Some(nestedMap) =>
                        nestedMap.key(
                          "required",
                          entry => {
                            val required = ScalarNode(entry.value).boolean().value.asInstanceOf[Boolean]
                            property.set(PropertyShapeModel.MinCount,
                                         AmfScalar(if (required) 1 else 0),
                                         Annotations(entry) += ExplicitField())
                          }
                        )
                      case _ =>
                    }
                    property.add(Annotations(e)).withRange(s).adopted(property.id)
                }
            })
            webFormShape
              .set(NodeShapeModel.Closed, value = true) // RAML 0.8 does not support open node shapes (see APIMF-732)
            webFormShape
        }
      })
  }
}

abstract class RamlPayloadParser(entry: YMapEntry,
                                 producer: Option[String] => Payload,
                                 parseOptional: Boolean = false)(implicit ctx: RamlWebApiContext) {

  def parse(): Payload = producer(Some(entry.key)).add(Annotations(entry))
}
