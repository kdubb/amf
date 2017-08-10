package amf.model

import amf.model.builder.RequestBuilder

import scala.collection.JavaConverters._

/**
  * Request jvm class
  */
case class Request private[model] (private[amf] val request: amf.domain.Request) extends DomainElement {

  val queryParameters: java.util.List[Parameter] = request.queryParameters.map(Parameter).asJava

  val headers: java.util.List[Parameter] = request.headers.map(Parameter).asJava

  val payloads: java.util.List[Payload] = request.payloads.map(Payload).asJava

  def toBuilder: RequestBuilder = RequestBuilder(request.toBuilder)

  override def equals(other: Any): Boolean = other match {
    case that: Request =>
      (that canEqual this) &&
        request == that.request
    case _ => false
  }

  def canEqual(other: Any): Boolean = other.isInstanceOf[Request]

}
