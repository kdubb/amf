package amf.plugins.document.webapi.parser.spec.async.emitters

import amf.core.emitter.BaseEmitters._
import amf.core.emitter.{EntryEmitter, PartEmitter, SpecOrdering}
import amf.core.model.domain.{AmfScalar, Shape}
import amf.core.parser.Position.ZERO
import amf.core.parser.{FieldEntry, Position}
import amf.plugins.document.webapi.annotations.ExampleIndex
import amf.plugins.document.webapi.contexts.emitter.OasLikeSpecEmitterContext
import amf.plugins.document.webapi.parser.spec.{OasDefinitions, SpecContextShapeAdapter}
import amf.plugins.document.webapi.parser.spec.declaration.{OasTagToReferenceEmitter, emitters}
import amf.plugins.document.webapi.parser.spec.declaration.emitters.async
import amf.plugins.document.webapi.parser.spec.declaration.emitters.async.AsyncSchemaEmitter
import amf.plugins.document.webapi.parser.spec.domain.{ExampleDataNodePartEmitter, NamedMultipleExampleEmitter}
import amf.plugins.document.webapi.parser.spec.oas.emitters.{OasLikeExampleEmitters, TagsEmitter}
import amf.plugins.domain.shapes.models.Example
import amf.plugins.domain.webapi.annotations.OrphanOasExtension
import amf.plugins.domain.webapi.metamodel.{MessageModel, PayloadModel}
import amf.plugins.domain.webapi.models._
import org.yaml.model.YDocument.{EntryBuilder, PartBuilder}
import org.yaml.model.{YDocument, YNode}

import scala.collection.mutable.ListBuffer

class AsyncApiMessageEmitter(fieldEntry: FieldEntry, ordering: SpecOrdering)(
    implicit val spec: OasLikeSpecEmitterContext)
    extends EntryEmitter {
  override def emit(b: YDocument.EntryBuilder): Unit = {
    val messages = fieldEntry.arrayValues[Message]
    messages.size match {
      case 1          => emitSingle(b, messages.head)
      case s if s > 1 => emitMultiple(b)
      case _          => Unit
    }
  }

  private def emitSingle(b: YDocument.EntryBuilder, message: Message): Unit = {
    val emitter = new AsyncApiMessageContentEmitter(message, ordering = ordering)
    b.complexEntry(
      ScalarEmitter(AmfScalar("message")).emit,
      emitter.emit
    )
  }

  private def emitMultiple(b: YDocument.EntryBuilder): Unit = {
    b.entry(
      YNode("message"),
      _.obj(new AsyncApiOneOfMessageEmitter(fieldEntry, ordering).emit)
    )
  }

  override def position(): Position = pos(fieldEntry.value.annotations)
}

case class AsyncMessageDeclarationsEmitter(messages: Seq[Message], isTrait: Boolean, ordering: SpecOrdering)(
    implicit spec: OasLikeSpecEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    b.entry(
      if (isTrait) "messageTraits" else "messages",
      _.obj(entryBuilder => {
        messages.foreach(msg => {
          val emitter = new AsyncApiMessageContentEmitter(msg, isTrait = isTrait, ordering)
          entryBuilder.entry(msg.name.value(), b => emitter.emit(b))
        })
      })
    )
  }

  override def position(): Position = messages.headOption.map(p => pos(p.annotations)).getOrElse(ZERO)
}

case class AsyncTraitMessagesEmitter(messages: Seq[Message], ordering: SpecOrdering)(
    implicit spec: OasLikeSpecEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    b.entry(
      "traits",
      _.list(partBuilder => {
        messages.foreach(msg => {
          val emitter = new AsyncApiMessageContentEmitter(msg, isTrait = true, ordering)
          emitter.emit(partBuilder)
        })
      })
    )
  }
  override def position(): Position = messages.headOption.map(p => pos(p.annotations)).getOrElse(ZERO)
}

private class AsyncApiOneOfMessageEmitter(fieldEntry: FieldEntry, ordering: SpecOrdering)(
    implicit val spec: OasLikeSpecEmitterContext)
    extends EntryEmitter {
  override def emit(b: YDocument.EntryBuilder): Unit = {
    val messages: Seq[Message] = fieldEntry.arrayValues[Message]
    val emitters               = messages.map(x => new AsyncApiMessageContentEmitter(x, ordering = ordering))
    b.entry(
      YNode("oneOf"),
      _.list(traverse(ordering.sorted(emitters), _))
    )
  }

  override def position(): Position = pos(fieldEntry.value.annotations)
}

class AsyncApiMessageContentEmitter(message: Message, isTrait: Boolean = false, ordering: SpecOrdering)(
    implicit val spec: OasLikeSpecEmitterContext)
    extends PartEmitter {

  private implicit val shapeCtx = SpecContextShapeAdapter(spec)

  override def emit(b: YDocument.PartBuilder): Unit = {
    val fs = message.fields
    sourceOr(
      message.annotations,
      if (message.isLink)
        emitLink(b)
      else {
        b.obj {
          emitter =>
            {
              val result = ListBuffer[EntryEmitter]()
              fs.entry(MessageModel.DisplayName).map(f => result += ValueEmitter("name", f))
              val bindingOrphanAnnotations =
                message.customDomainProperties.filter(_.extension.annotations.contains(classOf[OrphanOasExtension]))
              fs.entry(MessageModel.HeaderSchema).foreach(emitHeader(result, _))
              fs.entry(MessageModel.CorrelationId)
                .map(f => result += new AsyncApiCorrelationIdEmitter(f.element.asInstanceOf[CorrelationId], ordering))
              fs.entry(MessageModel.Title).map(f => result += ValueEmitter("title", f))
              fs.entry(MessageModel.Summary).map(f => result += ValueEmitter("summary", f))
              fs.entry(MessageModel.Description).map(f => result += ValueEmitter("description", f))
              fs.entry(MessageModel.Tags)
                .map(f => result += TagsEmitter("tags", f.array.values.asInstanceOf[Seq[Tag]], ordering))
              fs.entry(MessageModel.Documentation)
                .map(f => result += new AsyncApiCreativeWorksEmitter(f.element.asInstanceOf[CreativeWork], ordering))
              fs.entry(MessageModel.Bindings)
                .foreach(f => result += new AsyncApiBindingsEmitter(f.value.value, ordering, bindingOrphanAnnotations))

              val headerExamples  = fs.entry(MessageModel.HeaderExamples).map(_.arrayValues[Example]).getOrElse(Nil)
              val payloadExamples = fs.entry(MessageModel.Examples).map(_.arrayValues[Example]).getOrElse(Nil)
              if (payloadExamples.nonEmpty || headerExamples.nonEmpty)
                result += MessageExamplesEmitter(headerExamples, payloadExamples, ordering)

              if (!isTrait) {
                fs.entry(MessageModel.Extends).foreach(f => emitTraits(f, result))
                fs.entry(MessageModel.Payloads).foreach(f => emitPayloads(f, result))
              }
              traverse(ordering.sorted(result), emitter)
            }
        }
      }
    )
  }

  def emitLink(b: PartBuilder): Unit = OasTagToReferenceEmitter(message).emit(b)

  def emitTraits(f: FieldEntry, result: ListBuffer[EntryEmitter]): Unit = {
    result += AsyncTraitMessagesEmitter(f.arrayValues[Message], ordering)
  }

  private def emitHeader(result: ListBuffer[EntryEmitter], field: FieldEntry): Unit = {
    result += async.AsyncSchemaEmitter("headers", field.element.asInstanceOf[Shape], ordering, Seq())
  }

  private def emitPayloads(f: FieldEntry, result: ListBuffer[EntryEmitter]): Unit = {
    f.arrayValues[Payload].headOption.foreach { payload =>
      val fs              = payload.fields
      val schemaMediaType = payload.schemaMediaType.option()
      fs.entry(PayloadModel.MediaType).map(field => result += ValueEmitter("contentType", field))
      fs.entry(PayloadModel.SchemaMediaType).map(field => result += ValueEmitter("schemaFormat", field))
      fs.entry(PayloadModel.Schema)
        .map(field =>
          result += async
            .AsyncSchemaEmitter("payload", field.element.asInstanceOf[Shape], ordering, List(), schemaMediaType))
    }
  }

  override def position(): Position = pos(message.annotations)
}

case class MessageExamplesEmitter(headerExamples: Seq[Example], payloadExamples: Seq[Example], ordering: SpecOrdering)(
    implicit val spec: OasLikeSpecEmitterContext)
    extends EntryEmitter {

  override def emit(b: YDocument.EntryBuilder): Unit = {
    b.entry(
      "examples",
      b => {
        b.list { listBuilder =>
          val pairs = findExamplePairs(headerExamples, payloadExamples, maxIndex = headerExamples.size + payloadExamples.size)
          val pairEmitters = pairs.map { MessageExamplePairEmitter(_, ordering) }
          traverse(ordering.sorted(pairEmitters), listBuilder)
        }
      }
    )
  }

  private def findExamplePairs(headerExamples: Seq[Example],
                               payloadExamples: Seq[Example],
                               pairIndex: Int = 0,
                               maxIndex: Int,
  ): Seq[MessageExamplePair] = {
    if ((headerExamples.isEmpty && payloadExamples.isEmpty) || pairIndex >= maxIndex) Nil
    else {
      val (headerExample, updatedHeaderExamples)   = findAndRemoveExampleOfIndex(headerExamples, pairIndex)
      val (payloadExample, updatedPayloadExamples) = findAndRemoveExampleOfIndex(payloadExamples, pairIndex)
      MessageExamplePair(headerExample, payloadExample) +: findExamplePairs(updatedHeaderExamples,
                                                          updatedPayloadExamples,
                                                          pairIndex + 1,
                                                          maxIndex)
    }
  }

  private def findAndRemoveExampleOfIndex(examples: Seq[Example], pairIndex: Int): (Option[Example], Seq[Example]) = {
    val example = examples.find(_.annotations.find { a =>
      a match {
        case ExampleIndex(i) => i == pairIndex
        case _               => false
      }
    }.isDefined)
    example match {
      case Some(e) => (Some(e), examples.filterNot(_ == e))
      case None    => (None, examples)
    }
  }

  override def position(): Position =
    (headerExamples ++: payloadExamples).headOption.map(ex => pos(ex.annotations)).getOrElse(Position.ZERO)
}

case class MessageExamplePair(headerExample: Option[Example], payloadExample: Option[Example])

case class MessageExamplePairEmitter(pair: MessageExamplePair,
                                     ordering: SpecOrdering)(implicit val spec: OasLikeSpecEmitterContext)
    extends PartEmitter {

  private implicit val shapeCtx = SpecContextShapeAdapter(spec)

  override def emit(b: YDocument.PartBuilder): Unit = {
    b.obj { entryBuilder =>
      val emitters: List[EntryEmitter] = List("headers" -> pair.headerExample, "payload" -> pair.payloadExample).flatMap {
        case (key, example) =>
          example.map { ex =>
            EntryPartEmitter(key, ExampleDataNodePartEmitter(ex, ordering), position = pos(ex.annotations))
          }
      }
      traverse(ordering.sorted(emitters), entryBuilder)
    }
  }

  override def position(): Position =
    pair.headerExample.orElse(pair.payloadExample).map(ex => pos(ex.annotations)).getOrElse(Position.ZERO)
}
