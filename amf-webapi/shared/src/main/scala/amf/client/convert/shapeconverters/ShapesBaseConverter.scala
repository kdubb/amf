package amf.client.convert.shapeconverters

import amf.client.convert.{BidirectionalMatcher, CoreBaseConverter}
import amf.client.model.domain.{shapes, CreativeWork => ClientCreativeWork}
import amf.core.unsafe.PlatformSecrets
import amf.plugins.domain.shapes.models.{SchemaShape, _}
import amf.plugins.domain.webapi.models.IriTemplateMapping

trait ShapesBaseConverter
    extends CoreBaseConverter
    with NilShapeConverter
    with SchemaShapeConverter
    with NodeShapeConverter
    with ScalarShapeConverter
    with FileShapeConverter
    with AnyShapeConverter
    with ArrayShapeConverter
    with TupleShapeConverter
    with XMLSerializerConverter
    with ExampleConverter
    with UnionShapeConverter
    with PropertyDependenciesConverter
    with SchemaDependenciesConverter
    with CreativeWorkConverter
    with IriTemplateMappingConverter

trait NilShapeConverter extends PlatformSecrets {

  implicit object NilShapeMatcher extends BidirectionalMatcher[NilShape, shapes.NilShape] {
    override def asClient(from: NilShape): shapes.NilShape   = platform.wrap[shapes.NilShape](from)
    override def asInternal(from: shapes.NilShape): NilShape = from._internal
  }
}

trait SchemaShapeConverter extends PlatformSecrets {

  implicit object SchemaShapeMatcher extends BidirectionalMatcher[SchemaShape, shapes.SchemaShape] {
    override def asClient(from: SchemaShape): shapes.SchemaShape   = platform.wrap[shapes.SchemaShape](from)
    override def asInternal(from: shapes.SchemaShape): SchemaShape = from._internal
  }
}

trait NodeShapeConverter extends PlatformSecrets {

  implicit object NodeShapeMatcher extends BidirectionalMatcher[NodeShape, shapes.NodeShape] {
    override def asClient(from: NodeShape): shapes.NodeShape   = platform.wrap[shapes.NodeShape](from)
    override def asInternal(from: shapes.NodeShape): NodeShape = from._internal
  }
}

trait ScalarShapeConverter extends PlatformSecrets {

  implicit object ScalarShapeMatcher extends BidirectionalMatcher[ScalarShape, shapes.ScalarShape] {
    override def asClient(from: ScalarShape): shapes.ScalarShape   = platform.wrap[shapes.ScalarShape](from)
    override def asInternal(from: shapes.ScalarShape): ScalarShape = from._internal
  }
}

trait FileShapeConverter extends PlatformSecrets {

  implicit object FileShapeMatcher extends BidirectionalMatcher[FileShape, shapes.FileShape] {
    override def asClient(from: FileShape): shapes.FileShape   = platform.wrap[shapes.FileShape](from)
    override def asInternal(from: shapes.FileShape): FileShape = from._internal
  }
}

trait AnyShapeConverter extends PlatformSecrets {

  implicit object AnyShapeMatcher extends BidirectionalMatcher[AnyShape, shapes.AnyShape] {
    override def asClient(from: AnyShape): shapes.AnyShape   = platform.wrap[shapes.AnyShape](from)
    override def asInternal(from: shapes.AnyShape): AnyShape = from._internal
  }
}

trait ArrayShapeConverter extends PlatformSecrets {

  implicit object ArrayShapeMatcher extends BidirectionalMatcher[ArrayShape, shapes.ArrayShape] {
    override def asClient(from: ArrayShape): shapes.ArrayShape   = platform.wrap[shapes.ArrayShape](from)
    override def asInternal(from: shapes.ArrayShape): ArrayShape = from._internal
  }
}

trait TupleShapeConverter extends PlatformSecrets {

  implicit object TupleShapeMatcher extends BidirectionalMatcher[TupleShape, shapes.TupleShape] {
    override def asClient(from: TupleShape): shapes.TupleShape   = platform.wrap[shapes.TupleShape](from)
    override def asInternal(from: shapes.TupleShape): TupleShape = from._internal
  }
}

trait XMLSerializerConverter extends PlatformSecrets {

  implicit object XMLSerializerMatcher extends BidirectionalMatcher[XMLSerializer, shapes.XMLSerializer] {
    override def asClient(from: XMLSerializer): shapes.XMLSerializer   = platform.wrap[shapes.XMLSerializer](from)
    override def asInternal(from: shapes.XMLSerializer): XMLSerializer = from._internal
  }
}

trait ExampleConverter extends PlatformSecrets {

  implicit object ExampleMatcher extends BidirectionalMatcher[Example, shapes.Example] {
    override def asClient(from: Example): shapes.Example   = platform.wrap[shapes.Example](from)
    override def asInternal(from: shapes.Example): Example = from._internal
  }
}

trait UnionShapeConverter extends PlatformSecrets {
  implicit object UnionShapeMatcher extends BidirectionalMatcher[UnionShape, shapes.UnionShape] {
    override def asClient(from: UnionShape): shapes.UnionShape   = platform.wrap[shapes.UnionShape](from)
    override def asInternal(from: shapes.UnionShape): UnionShape = from._internal
  }
}

trait PropertyDependenciesConverter extends PlatformSecrets {

  implicit object PropertyDependenciesMatcher
      extends BidirectionalMatcher[PropertyDependencies, shapes.PropertyDependencies] {
    override def asClient(from: PropertyDependencies): shapes.PropertyDependencies =
      platform.wrap[shapes.PropertyDependencies](from)
    override def asInternal(from: shapes.PropertyDependencies): PropertyDependencies = from._internal
  }
}

trait SchemaDependenciesConverter extends PlatformSecrets {

  implicit object SchemaDependenciesMatcher
      extends BidirectionalMatcher[SchemaDependencies, shapes.SchemaDependencies] {
    override def asClient(from: SchemaDependencies): shapes.SchemaDependencies =
      platform.wrap[shapes.SchemaDependencies](from)
    override def asInternal(from: shapes.SchemaDependencies): SchemaDependencies = from._internal
  }
}

trait CreativeWorkConverter extends PlatformSecrets {

  implicit object CreativeWorkMatcher extends BidirectionalMatcher[CreativeWork, ClientCreativeWork] {
    override def asClient(from: CreativeWork): ClientCreativeWork   = platform.wrap[ClientCreativeWork](from)
    override def asInternal(from: ClientCreativeWork): CreativeWork = from._internal
  }
}

trait IriTemplateMappingConverter extends PlatformSecrets {

  implicit object IriTemplateMappingConverter
      extends BidirectionalMatcher[IriTemplateMapping, shapes.IriTemplateMapping] {
    override def asClient(from: IriTemplateMapping): shapes.IriTemplateMapping =
      platform.wrap[shapes.IriTemplateMapping](from)
    override def asInternal(from: shapes.IriTemplateMapping): IriTemplateMapping = from._internal
  }
}
