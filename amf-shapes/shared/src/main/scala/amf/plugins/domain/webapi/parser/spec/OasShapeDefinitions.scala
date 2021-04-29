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

  protected def appendPrefix(prefix: String, url: String): String = prefix + url
}

object OasShapeDefinitions extends OasShapeDefinitions
