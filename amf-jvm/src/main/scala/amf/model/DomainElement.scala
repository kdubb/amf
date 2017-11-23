package amf.model

import amf.framework.model.domain
import amf.plugins.domain.webapi.models

import scala.collection.JavaConverters._

/**
  * Domain element.
  */
trait DomainElement {

  private[amf] def element: domain.DomainElement

  lazy val customDomainProperties: java.util.List[DomainExtension] =
    element.customDomainProperties.map(DomainExtension).asJava
  lazy val `extends`: java.util.List[DomainElement] = element.extend.map {
    case pd: amf.domain.`abstract`.ParametrizedDeclaration => ParametrizedDeclaration(pd)
    case op: amf.domain.Operation                          => Operation(op)
    case e: amf.domain.EndPoint                            => EndPoint(e)
  }.asJava

  def withCustomDomainProperties(customProperties: java.util.List[DomainExtension]): this.type = {
    element.withCustomDomainProperties(customProperties.asScala.map(_.domainExtension))
    this
  }

  def withExtends(extend: java.util.List[ParametrizedDeclaration]): this.type = {
    element.withExtends(extend.asScala.map(_.element))
    this
  }

  def withResourceType(name: String): ParametrizedResourceType =
    ParametrizedResourceType(element.withResourceType(name))

  def withTrait(name: String): ParametrizedTrait = ParametrizedTrait(element.withTrait(name))

  def position(): amf.parser.Range = element.position() match {
    case Some(pos) => pos
    case _         => null
  }

  // API for direct property manipulation

  def getId(): String = element.id

  def getTypeIds(): java.util.List[String] = element.getTypeIds().asJava

  def getPropertyIds(): java.util.List[String] = element.getPropertyIds().asJava

  def getScalarByPropertyId(propertyId: String): java.util.List[Object] =
    element.getScalarByPropertyId(propertyId).map(_.asInstanceOf[Object]).asJava

  def getObjectByPropertyId(propertyId: String): java.util.List[DomainElement] =
    element.getObjectByPropertyId(propertyId).map(d => DomainElement(d)).asJava
}

object DomainElement {
  def apply(domainElement: domain.DomainElement): DomainElement = domainElement match {
    case o: models.WebApi                                  => WebApi(o)
    case o: amf.domain.Operation                           => Operation(o)
    case o: amf.domain.Organization                        => Organization(o)
    case o: amf.domain.ExternalDomainElement               => throw new Exception("Not supported yet")
    case o: amf.domain.Parameter                           => Parameter(o)
    case o: amf.domain.Payload                             => Payload(o)
    case o: amf.domain.CreativeWork                        => CreativeWork(o)
    case o: amf.domain.EndPoint                            => EndPoint(o)
    case o: amf.domain.Request                             => Request(o)
    case o: amf.domain.Response                            => Response(o)
    case o: amf.domain.security.ParametrizedSecurityScheme => ParametrizedSecurityScheme(o)
    case o: amf.domain.security.SecurityScheme             => SecurityScheme(o)
    case o: amf.domain.extensions.ObjectNode               => ObjectNode(o)
    case o: amf.domain.extensions.ScalarNode               => ScalarNode(o)
    case o: amf.domain.extensions.CustomDomainProperty     => CustomDomainProperty(o)
    case o: amf.domain.extensions.ArrayNode                => ArrayNode(o)
    case o: amf.domain.extensions.DomainExtension          => DomainExtension(o)
    case o: amf.shape.Shape                                => Shape(o)
    case o: amf.domain.dialects.DomainEntity               => DomainEntity(o)
    case o =>
      new DomainElement {
        override private[amf] def element = o
      }
  }
}

trait Linkable { this: DomainElement with Linkable =>

  private[amf] def element: domain.DomainElement with amf.domain.Linkable

  def linkTarget: Option[DomainElement with Linkable]

  def isLink: Boolean           = linkTarget.isDefined
  def linkLabel: Option[String] = element.linkLabel

  def linkCopy(): DomainElement with Linkable

  def withLinkTarget(target: DomainElement with Linkable): this.type = {
    element.withLinkTarget(target.element)
    this
  }

  def withLinkLabel(label: String): this.type = {
    element.withLinkLabel(label)
    this
  }

  def link[T](label: Option[String] = None): T = {
    val href = linkCopy()
    href.withLinkTarget(this)
    label.map(href.withLinkLabel)

    href.asInstanceOf[T]
  }
}
