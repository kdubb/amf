package amf.plugins.document.webapi.parser.spec.oas.emitters

import amf.core.model.domain.DomainElement
import amf.plugins.document.webapi.parser.spec.declaration.emitters.DefinitionsQueue

import scala.util.matching.Regex

trait CompactEmissionContext {
  //  regex used to validate if the name of the shape is a valid label for referencing and declaring in definitions
  val nameRegex: Regex = """^[^/]+$""".r

  val definitionsQueue: DefinitionsQueue = DefinitionsQueue()(this)

  var forceEmission: Option[String] = None

  // oas emission emits schemas to the definitions, so we need the schemas to emit all their examples
  def filterLocal[T <: DomainElement](elements: Seq[T]): Seq[T] = elements
}
