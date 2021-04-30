package amf.plugins.document.webapi.parser.spec.raml

import amf.core.parser.YMapOps
import org.yaml.model.{IllegalTypeHandler, YMap, YMapEntry, YType}

trait RamlShapeParser {

  protected def typeOrSchema(map: YMap): Option[YMapEntry] = map.key("type").orElse(map.key("schema"))

  protected def nestedTypeOrSchema(map: YMap): Option[YMapEntry] = map.key("type").orElse(map.key("schema")) match {
    case Some(n) if n.value.tagType == YType.Map =>
      implicit val eh = IllegalTypeHandler.returnDefault
      nestedTypeOrSchema(n.value.as[YMap])
    case res =>
      res
  }
}
