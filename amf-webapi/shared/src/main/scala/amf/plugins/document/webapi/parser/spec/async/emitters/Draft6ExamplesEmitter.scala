package amf.plugins.document.webapi.parser.spec.async.emitters

import amf.core.emitter.BaseEmitters.{EntryPartEmitter, pos}
import amf.core.emitter.{EntryEmitter, PartEmitter, SpecOrdering}
import amf.core.model.document.BaseUnit
import amf.core.parser.Position
import amf.plugins.document.webapi.contexts.emitter.OasLikeSpecEmitterContext
import amf.plugins.document.webapi.parser.spec.SpecContextShapeAdapter
import amf.plugins.document.webapi.parser.spec.domain.ExampleDataNodePartEmitter
import amf.plugins.document.webapi.parser.spec.oas.emitters.OasLikeExampleEmitters
import amf.plugins.domain.shapes.models.Example
import amf.plugins.domain.webapi.parser.spec.declaration.emitters.annotations.DataNodeEmitter
import org.yaml.model.YDocument

import scala.collection.mutable.ListBuffer

case class Draft6ExamplesEmitter(examples: Seq[Example], ordering: SpecOrdering)(
    implicit spec: OasLikeSpecEmitterContext)
    extends OasLikeExampleEmitters
    with EntryEmitter {

  private implicit val shapeCtx = SpecContextShapeAdapter(spec)

  private def entryEmitter: EntryEmitter =
    EntryPartEmitter("examples",
                     ExamplesArrayPartEmitter(examples, ordering),
                     position = examples.headOption.map(h => pos(h.annotations)).getOrElse(Position.ZERO))

  override def emit(b: YDocument.EntryBuilder): Unit = entryEmitter.emit(b)

  override def emitters(): ListBuffer[EntryEmitter] = ListBuffer(entryEmitter)

  override def position(): Position = examples.headOption.map(ex => pos(ex.annotations)).getOrElse(Position.ZERO)
}

case class ExamplesArrayPartEmitter(examples: Seq[Example], ordering: SpecOrdering)(
    implicit spec: OasLikeSpecEmitterContext)
    extends PartEmitter {

  private implicit val shapeCtx = SpecContextShapeAdapter(spec)

  override def emit(b: YDocument.PartBuilder): Unit = {
    b.list { listBuilder =>
      examples.foreach(ex => ExampleDataNodePartEmitter(ex, ordering).emit(listBuilder))
    }
  }
  override def position(): Position = examples.headOption.map(ex => pos(ex.annotations)).getOrElse(Position.ZERO)
}
