package amf.plugins.document.webapi.parser.spec.declaration

import amf.core.emitter.BaseEmitters._
import amf.core.model.domain.DomainElement
import amf.core.parser.Position
import amf.plugins.document.webapi.contexts.emitter.OasLikeSpecEmitterContext
import amf.plugins.document.webapi.parser.spec.OasDefinitions._
import amf.plugins.document.webapi.parser.spec.SpecContextShapeAdapter
import amf.plugins.document.webapi.parser.spec.oas.OasSpecEmitter
import amf.plugins.domain.webapi.models.bindings.{ChannelBindings, MessageBindings, OperationBindings, ServerBindings}
import amf.plugins.domain.webapi.models._

/**
  *
  */
case class OasTagToReferenceEmitter(link: DomainElement)(implicit val specContext: OasLikeSpecEmitterContext)
    extends OasSpecEmitter
    with ShapeReferenceEmitter {

  protected override val shapeSpec = SpecContextShapeAdapter(specContext)

  override protected def getRefUrlFor(element: DomainElement, default: String = referenceLabel) = element match {
    case _: Parameter                        => appendParameterDefinitionsPrefix(referenceLabel)
    case _: Payload                          => appendParameterDefinitionsPrefix(referenceLabel)
    case _: Response                         => appendResponsesDefinitionsPrefix(referenceLabel)
    case _: Callback                         => appendOas3ComponentsPrefix(referenceLabel, "callbacks")
    case _: TemplatedLink                    => appendOas3ComponentsPrefix(referenceLabel, "links")
    case _: CorrelationId                    => appendOas3ComponentsPrefix(referenceLabel, "correlationIds")
    case m: Message if m.isAbstract.value()  => appendOas3ComponentsPrefix(referenceLabel, "messageTraits")
    case m: Message if !m.isAbstract.value() => appendOas3ComponentsPrefix(referenceLabel, "messages")
    case _: ServerBindings                   => appendOas3ComponentsPrefix(referenceLabel, "serverBindings")
    case _: OperationBindings                => appendOas3ComponentsPrefix(referenceLabel, "operationBindings")
    case _: ChannelBindings                  => appendOas3ComponentsPrefix(referenceLabel, "channelBindings")
    case _: MessageBindings                  => appendOas3ComponentsPrefix(referenceLabel, "messageBindings")
    case _                                   => super.getRefUrlFor(element, default)
  }

  override def position(): Position = pos(link.annotations)
}
