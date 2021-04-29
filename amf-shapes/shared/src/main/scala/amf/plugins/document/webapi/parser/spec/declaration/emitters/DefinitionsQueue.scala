package amf.plugins.document.webapi.parser.spec.declaration.emitters

import amf.core.model.domain.Shape
import amf.core.utils.IdCounter
import amf.plugins.document.webapi.parser.spec.declaration.ShapeEmitterContext
import amf.plugins.document.webapi.parser.spec.declaration.emitters.AliasDefinitions.{Id, Label}
import amf.plugins.document.webapi.parser.spec.oas.emitters.CompactEmissionContext

import scala.collection.mutable

case class DefinitionsQueue(pendingEmission: mutable.Queue[LabeledShape] = new mutable.Queue(),
                             queuedIdsWithLabel: mutable.Map[Id, Label] = mutable.Map[String, String]())(ctx: CompactEmissionContext) {

  def enqueue(shape: Shape): String =
    queuedIdsWithLabel.getOrElse( // if the shape has already been queued the assigned label is returned
      shape.id, {
        val label        = createLabel(shape)
        val labeledShape = LabeledShape(label, shape)
        pendingEmission += labeledShape
        queuedIdsWithLabel += labeledShape.shape.id -> labeledShape.label
        labeledShape.label
      }
    )

  def labelOfShape(id: String): Option[String] = queuedIdsWithLabel.get(id)
  def nonEmpty(): Boolean                      = pendingEmission.nonEmpty
  def dequeue(): LabeledShape                  = pendingEmission.dequeue

  val counter = new IdCounter()

  private def createLabel(shape: Shape): String = {
    val name: String = normalizeName(shape.name.option())
    if (queuedIdsWithLabel.valuesIterator.contains(name)) counter.genId(name) else name
  }
  def normalizeName(name: Option[String]): String = name.filter(isValidName).getOrElse("default")
  private def isValidName(s: String): Boolean     = ctx.nameRegex.pattern.matcher(s).matches()
}

case class LabeledShape(label: String, shape: Shape)

object AliasDefinitions {
  type Label = String
  type Id    = String
}

