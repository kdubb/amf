package amf.plugins.document.webapi.parser.spec.async.parser

import amf.core.annotations.{DefinedByVendor, SourceVendor}
import amf.core.errorhandling.ErrorHandler
import amf.core.model.domain.{AmfScalar, Shape}
import amf.core.parser.{Annotations, SearchScope}
import amf.core.remote.Raml10
import amf.plugins.document.webapi.contexts.parser.OasLikeWebApiContext
import amf.plugins.document.webapi.contexts.parser.adapters.WebApiAdapterShapeParserContext
import amf.plugins.document.webapi.parser.spec.common.YMapEntryLike
import amf.plugins.document.webapi.parser.spec.declaration.SchemaPosition._
import amf.plugins.document.webapi.parser.spec.declaration._
import amf.plugins.document.webapi.parser.spec.declaration.emitters.async.AsyncApiKnownSchemaFormats
import amf.plugins.document.webapi.parser.spec.{WebApiDeclarations, toRaml}
import amf.plugins.domain.webapi.models.Payload
import amf.plugins.domain.webapi.parser.spec.declaration.TypeInfo
import amf.plugins.features.validation.CoreValidations
import org.yaml.model.YMapEntry

object AsyncSchemaFormats extends AsyncApiKnownSchemaFormats {

  def getSchemaVersion(payload: Payload)(implicit errorHandler: ErrorHandler): SchemaVersion = {
    val value = Option(payload.schemaMediaType).map(f => f.value()).orElse(None)
    getSchemaVersion(value)
  }
}

case class AsyncApiTypeParser(entry: YMapEntry, adopt: Shape => Unit, version: SchemaVersion)(
    implicit val ctx: OasLikeWebApiContext) {

  def parse(): Option[Shape] = version match {
    case RAML10SchemaVersion() => CustomRamlReferenceParser(YMapEntryLike(entry), adopt).parse()
    case _                     => OasTypeParser(entry, adopt, version)(WebApiAdapterShapeParserContext(ctx)).parse()
  }
}

case class CustomRamlReferenceParser(entry: YMapEntryLike, adopt: Shape => Unit)(
    implicit val ctx: OasLikeWebApiContext) {

  def parse(): Option[Shape] = {
    val shape = ctx.link(entry.value) match {
      case Left(refValue) => handleRef(refValue)
      case Right(_)       => parseRamlType(entry)
    }
    shape.foreach(_.annotations += DefinedByVendor(Raml10))
    shape
  }

  private def parseRamlType(entry: YMapEntryLike): Option[Shape] = {
    val context = toRaml(ctx)
    context.declarations.shapes = Map.empty
    val result =
      Raml10TypeParser(entry, "schema", adopt, TypeInfo(), AnyDefaultType)(WebApiAdapterShapeParserContext(context))
        .parse()
    context.futureDeclarations.resolve()
    result
  }

  private def handleRef(refValue: String): Option[Shape] = {
    val link = dataTypeFragmentRef(refValue)
      .orElse(typeDefinedInLibraryRef(refValue))
      .orElse(externalFragmentRef(refValue))

    if (link.isEmpty)
      ctx.eh.violation(CoreValidations.UnresolvedReference,
                       "",
                       s"Cannot find link reference $refValue",
                       entry.annotations)
    link
  }

  private def dataTypeFragmentRef(refValue: String): Option[Shape] = {
    val result = ctx.declarations.findType(refValue, SearchScope.Fragments)
    result.foreach(linkAndAdopt(_, refValue))
    result
  }

  private def typeDefinedInLibraryRef(refValue: String): Option[Shape] = {
    val values = refValue.split("#/types/").toList
    values match {
      case Seq(libUrl, typeName) =>
        val library = ctx.declarations.libraries.get(libUrl).collect { case d: WebApiDeclarations => d }
        val shape   = library.flatMap(_.shapes.get(typeName))
        shape.map(linkAndAdopt(_, refValue))
      case _ => None
    }
  }

  private def externalFragmentRef(refValue: String): Option[Shape] = {
    ctx.obtainRemoteYNode(refValue).flatMap { node =>
      parseRamlType(YMapEntryLike(node))
    }
  }

  private def linkAndAdopt(s: Shape, label: String): Shape = {
    val link = s.link(AmfScalar(label), entry.annotations, Annotations.synthesized()).asInstanceOf[Shape]
    adopt(link)
    link
  }
}
