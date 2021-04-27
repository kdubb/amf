package amf.plugins.domain.shapes.resolution.stages

import amf.ProfileName
import amf.core.errorhandling.ErrorHandler
import amf.core.model.document.BaseUnit
import amf.core.model.domain._
import amf.core.resolution.stages.ResolutionStage
import amf.core.resolution.stages.elements.resolution.ElementStageTransformer
import amf.core.resolution.stages.selectors.ShapeSelector
import amf.plugins.domain.shapes.resolution.stages.shape_normalization._

/**
  * Computes the canonical form for all the shapes in the model
  * We are assuming certain pre-conditions in the state of the shape:
  *  - All type references have been replaced by their expanded forms
  */
class ShapeNormalizationStage(profile: ProfileName, val keepEditingInfo: Boolean)(
    override implicit val errorHandler: ErrorHandler)
    extends ResolutionStage() {

  protected var m: Option[BaseUnit] = None
  protected val context             = new NormalizationContext(errorHandler, keepEditingInfo, profile)

  override def resolve[T <: BaseUnit](model: T): T = {
    m = Some(model)
    model.transform(ShapeSelector, transform).asInstanceOf[T]
  }

  protected def transform(element: DomainElement, isCycle: Boolean): Option[DomainElement] = {
    element match {
      case shape: Shape => transformer.transform(shape)
      case other        => Some(other)
    }
  }

  def transformer: ElementStageTransformer[Shape] = new ShapeTransformer(context)
}
