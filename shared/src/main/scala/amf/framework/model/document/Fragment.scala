package amf.framework.model.document

import amf.framework.metamodel.Obj
import amf.framework.metamodel.document.{BaseUnitModel, DocumentModel, FragmentModel}
import amf.framework.model.domain.{AmfObject, DomainElement}

/**
  * RAML Fragments
  */

/** Units encoding domain fragments */
trait Fragment extends BaseUnit with EncodesModel {

  /** Returns the list document URIs referenced from the document that has been parsed to generate this model */
  override val references: Seq[BaseUnit] = fields(DocumentModel.References)

  override def adopted(parent: String): this.type = withId(parent)

  override def usage: String = ""

  override def encodes: DomainElement = fields(FragmentModel.Encodes)

  override def location: String = fields(BaseUnitModel.Location)

  override def meta: Obj = FragmentModel
}

trait EncodesModel extends AmfObject {

  /** Encoded [[DomainElement]] described in the document element. */
  def encodes: DomainElement

  def withEncodes(encoded: DomainElement): this.type = set(FragmentModel.Encodes, encoded)
}
