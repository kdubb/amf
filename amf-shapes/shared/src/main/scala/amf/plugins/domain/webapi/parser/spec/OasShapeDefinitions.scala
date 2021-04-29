package amf.plugins.domain.webapi.parser.spec

import amf.core.remote.Vendor
import amf.plugins.document.webapi.parser.spec.oas.parser.types.ShapeParserContext

trait OasShapeDefinitions {

  val oas3ComponentsPrefix = "#/components/"

  val oas2DefinitionsPrefix = "#/definitions/"

  val oas3DefinitionsPrefix = "#/components/schemas/"

  def stripDefinitionsPrefix(url: String)(implicit ctx: ShapeParserContext): String = {
    if (ctx.vendor == Vendor.OAS30 || ctx.vendor == Vendor.ASYNC20) url.stripPrefix(oas3DefinitionsPrefix)
    else url.stripPrefix(oas2DefinitionsPrefix)
  }

  def stripOas3ComponentsPrefix(url: String, fieldName: String): String =
    url.stripPrefix(oas3ComponentsPrefix + fieldName + "/")

  def appendOas3ComponentsPrefix(url: String, fieldName: String): String = {
    appendPrefix(oas3ComponentsPrefix + s"$fieldName/", url)
  }

  def appendSchemasPrefix(url: String, vendor: Option[Vendor] = None): String = vendor match {
    case Some(Vendor.OAS30) | Some(Vendor.ASYNC20) =>
      if (!url.startsWith(oas3DefinitionsPrefix)) appendPrefix(oas3DefinitionsPrefix, url) else url
    case _ =>
      if (!url.startsWith(oas2DefinitionsPrefix)) appendPrefix(oas2DefinitionsPrefix, url) else url
  }

  protected def appendPrefix(prefix: String, url: String): String = prefix + url
}

object OasShapeDefinitions extends OasShapeDefinitions
