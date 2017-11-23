package amf.domain

import amf.framework.model.domain.DomainElement
import amf.framework.parser.Annotations
import amf.metadata.domain.LicenseModel
import amf.metadata.domain.LicenseModel.{Name, Url}
import org.yaml.model.YMap

/**
  * License internal model
  */
case class License(fields: Fields, annotations: Annotations) extends DomainElement {

  def url: String  = fields(Url)
  def name: String = fields(Name)

  def withUrl(url: String): this.type   = set(Url, url)
  def withName(name: String): this.type = set(Name, name)

  override def adopted(parent: String): this.type = withId(parent + "/license")

  override def meta = LicenseModel
}

object License {

  def apply(): License = apply(Annotations())

  def apply(ast: YMap): License = apply(Annotations(ast))

  def apply(annotations: Annotations): License = new License(Fields(), annotations)
}
