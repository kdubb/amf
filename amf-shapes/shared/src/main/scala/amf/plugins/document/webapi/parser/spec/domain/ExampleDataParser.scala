package amf.plugins.document.webapi.parser.spec.domain

import amf.core.metamodel.domain.ExternalSourceElementModel
import amf.core.model.domain.AmfScalar
import amf.core.parser.errorhandler.ParserErrorHandler
import amf.core.parser.{Annotations, YNodeLikeOps}
import amf.core.utils.IdCounter
import amf.plugins.document.webapi.annotations.ExternalReferenceUrl
import amf.plugins.document.webapi.parser.spec.oas.parser.types.ShapeParserContext
import amf.plugins.domain.shapes.metamodel.ExampleModel
import amf.plugins.domain.shapes.models.Example
import org.yaml.model.YNode.MutRef
import org.yaml.model.{YNode, YScalar, YSequence, YType}
import org.yaml.render.YamlRender

case class ExampleDataParser(node: YNode, example: Example, options: ExampleOptions)(implicit ctx: ShapeParserContext) {
  def parse(): Example = {
    if (example.fields.entry(ExampleModel.Strict).isEmpty) {
      example.set(ExampleModel.Strict, AmfScalar(options.strictDefault), Annotations.synthesized())
    }

    val (targetNode, mutTarget) = node match {
      case mut: MutRef =>
        val refUrl = mut.origValue.asInstanceOf[YScalar].text
        ctx.fragments
          .get(refUrl)
          .foreach { e =>
            example.add(ExternalReferenceUrl(refUrl))
            example.withReference(e.encoded.id)
            example.set(ExternalSourceElementModel.Location, e.location.getOrElse(ctx.loc))
          }
        (mut.target.getOrElse(node), true)
      case _ =>
        (node, false) // render always (even if xml) for | multiline strings. (If set scalar.text we lose the token)

    }

    node.toOption[YScalar] match {
      case Some(_) if node.tagType == YType.Null =>
        example.set(ExampleModel.Raw, AmfScalar("null", Annotations.valueNode(node)), Annotations.valueNode(node))
      case Some(scalar) =>
        example.set(ExampleModel.Raw, AmfScalar(scalar.text, Annotations.valueNode(node)), Annotations.valueNode(node))
      case _ =>
        example.set(ExampleModel.Raw,
                    AmfScalar(YamlRender.render(targetNode), Annotations.valueNode(node)),
                    Annotations.valueNode(node))

    }

    val result = NodeDataNodeParser(targetNode, example.id, options.quiet, mutTarget, options.isScalar).parse()

    result.dataNode.foreach { dataNode =>
      // If this example comes from a 08 param with type string, we force this to be a string
      example.set(ExampleModel.StructuredValue, dataNode, Annotations(node))
    }

    example
  }
}

case class ExamplesDataParser(seq: YSequence, options: ExampleOptions, parentId: String)(
    implicit ctx: ShapeParserContext) {

  implicit private val errorHandler: ParserErrorHandler = ctx.eh

  def parse(): Seq[Example] = {
    val counter = new IdCounter()
    seq.nodes.map { n =>
      val exa = Example(n).withName(counter.genId("default-example"))
      exa.adopted(parentId)
      ExampleDataParser(n, exa, options).parse()
    }
  }
}
