package amf.plugins.document.webapi.parser.spec.declaration.emitters.async

import amf.core.emitter.BaseEmitters.pos
import amf.core.emitter.{EntryEmitter, SpecOrdering}
import amf.core.errorhandling.ErrorHandler
import amf.core.model.document.BaseUnit
import amf.core.model.domain.Shape
import amf.core.parser.Position
import amf.plugins.document.webapi.parser.spec.declaration.SchemaPosition.Schema
import amf.plugins.document.webapi.parser.spec.declaration.emitters.oas.OasTypePartEmitter
import amf.plugins.document.webapi.parser.spec.declaration.emitters.raml.Raml10TypeEmitter
import amf.plugins.document.webapi.parser.spec.declaration._
import org.yaml.model.YDocument.EntryBuilder

trait AsyncApiKnownSchemaFormats {
  val async20Schema = List("application/vnd.aai.asyncapi;version=2.0.0",
                           "application/vnd.aai.asyncapi+json;version=2.0.0",
                           "application/vnd.aai.asyncapi+yaml;version=2.0.0")
  val oas30Schema = List("application/vnd.oai.openapi;version=3.0.0",
                         "application/vnd.oai.openapi+json;version=3.0.0",
                         "application/vnd.oai.openapi+yaml;version=3.0.0")
  val draft7JsonSchema = List("application/schema+json;version=draft-07", "application/schema+yaml;version=draft-07")
  val avroSchema = List("application/vnd.apache.avro;version=1.9.0",
                        "application/vnd.apache.avro+json;version=1.9.0",
                        "application/vnd.apache.avro+yaml;version=1.9.0")
  val ramlSchema = List(
    "application/raml+yaml;version=1.0"
  )

  def getSchemaVersion(value: Option[String])(implicit errorHandler: ErrorHandler): SchemaVersion =
    value match {
      case Some(format) if oas30Schema.contains(format) => OAS30SchemaVersion(Schema)
      case Some(format) if ramlSchema.contains(format)  => RAML10SchemaVersion()
      // async20 schemas are handled with draft 7. Avro schema is not supported
      case _ => JSONSchemaDraft7SchemaVersion
    }
}

case class AsyncSchemaEmitter(key: String,
                              shape: Shape,
                              ordering: SpecOrdering,
                              references: Seq[BaseUnit],
                              mediaType: Option[String] = None)(implicit spec: ShapeEmitterContext)
    extends EntryEmitter
    with AsyncApiKnownSchemaFormats {
  override def emit(b: EntryBuilder): Unit = {
    val schemaVersion = getSchemaVersion(mediaType)(spec.eh)
    schemaVersion match {
      case RAML10SchemaVersion() => emitAsRaml(b)
      case _                     => emitAsOas(b, schemaVersion)
    }
  }

  private def emitAsRaml(b: EntryBuilder): Unit = {
    val emitters = Raml10TypeEmitter(shape, ordering, references = references)(spec.toRamlNext).entries()
    b.entry(
      key,
      _.obj(eb => emitters.foreach(_.emit(eb)))
    )
  }

  private def emitAsOas(b: EntryBuilder, schemaVersion: SchemaVersion): Unit = {
    b.entry(
      key,
      b => {
        val newCtx = spec.toAsyncNext(schemaVersion)
        OasTypePartEmitter(shape, ordering, references = references)(newCtx).emit(b)
      }
    )
  }

  override def position(): Position = pos(shape.annotations)
}
