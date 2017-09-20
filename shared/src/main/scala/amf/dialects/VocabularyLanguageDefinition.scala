package amf.dialects

import amf.compiler.Root
import amf.vocabulary.{Namespace, ValueType}
/**
* Created by Pavel Petrochenko on 12/09/17.
*/

class VocabPartDialect(override val shortName:String, namespace: Namespace=Namespace.Meta) extends DialectNode(shortName, namespace) {}

case class Declaration(override val shortName:String,override val namespace: Namespace=Namespace.Meta) extends VocabPartDialect(shortName,namespace = namespace){
  val idProperty: DialectPropertyMapping = str("id",_.copy(namespace = Some(Namespace.Schema), jsonld = false, noRAML = true,scalaNameOverride = Some("idProperty")))
}


object ClassTerm extends Declaration("Class", Namespace.Owl){
  val displayName: DialectPropertyMapping  = str("displayName", _.copy(namespace = Some(Namespace.Schema), rdfName = Some("name")))
  val description: DialectPropertyMapping  = str("description", _.copy(namespace = Some(Namespace.Schema)))
  val `extends`: DialectPropertyMapping    = ref("extends",ClassTerm, _.copy(collection = true, jsonld = false))
  val `properties`: DialectPropertyMapping = ref("properties",PropertyTerm, _.copy(collection = true, jsonld = false))

  typeCalculator = {
    val calculator: TypeCalculator = {(domainEntity: DomainEntity) =>
      domainEntity.strings(`extends`).map(ValueType(_).asInstanceOf[ValueType]).toList
    }
    Some(calculator)
  }

}

object PropertyTerm extends Declaration("Property"){
  val description: DialectPropertyMapping  = str("description", _.copy(namespace = Some(Namespace.Schema)))
  val domain: DialectPropertyMapping       = ref("domain",ClassTerm, _.copy(collection = true, namespace = Some(Namespace.Rdfs)))
  val range: DialectPropertyMapping        = ref("range",ClassTerm, _.copy(collection = true, referenceTarget = Some(ClassTerm), namespace = Some(Namespace.Rdfs)))
  val `extends`: DialectPropertyMapping    = ref("extends",PropertyTerm, _.copy(collection = true))

  val DATATYPE_PROPERTY = ValueType("http://www.w3.org/2002/07/owl#DatatypeProperty")
  val OBJECT_PROPERTY   = ValueType("http://www.w3.org/2002/07/owl#ObjectProperty")


  fieldValueDiscriminator(range)
    .add(TypeBuiltins.ANY, DATATYPE_PROPERTY)
    .add(TypeBuiltins.STRING, DATATYPE_PROPERTY)
    .add(TypeBuiltins.INTEGER, DATATYPE_PROPERTY)
    .add(TypeBuiltins.NUMBER, DATATYPE_PROPERTY)
    .add(TypeBuiltins.FLOAT, DATATYPE_PROPERTY)
    .add(TypeBuiltins.BOOLEAN, DATATYPE_PROPERTY)
    .add(TypeBuiltins.URI, DATATYPE_PROPERTY)
    .defaultValue = Some(OBJECT_PROPERTY)
}

object External extends VocabPartDialect("External"){
  val name: DialectPropertyMapping = str("name")
  val uri: DialectPropertyMapping  = str("uri", _.copy(fromVal = true))
}

object Vocabulary extends VocabPartDialect("Vocabulary"){

  val base: DialectPropertyMapping             = str("base")
  val dialectProperty: DialectPropertyMapping  = str("dialect",_.copy(scalaNameOverride = Some("dialectProperty")))
  val version: DialectPropertyMapping          = str("version")
  var usage: DialectPropertyMapping            = str("usage", _.copy(namespace = Some(Namespace.Schema), rdfName = Some("description")))
  var externals: DialectPropertyMapping        = map("external", External.name, External,_.copy(scalaNameOverride = Some("externals")))
  var classTerms: DialectPropertyMapping       = map("classTerms", ClassTerm.idProperty, ClassTerm, _.copy(rdfName = Some("classes")))
  var propertyTerms: DialectPropertyMapping    = map("propertyTerms", PropertyTerm.idProperty,PropertyTerm, _.copy(rdfName = Some("properties")))

  withGlobalIdField("base")
  withType("http://www.w3.org/2002/07/owl#Ontology")

  nameProvider = {
    val provider: LocalNameProviderFactory = new BasicNameProvider(_, List(externals))
    Some(provider)
  }
}

class VocabularyRefiner extends Refiner{
  def refine(voc:DomainEntity): Unit = {
    for {
      classTerm    <- voc.entities(Vocabulary.classTerms)
      property     <- classTerm.strings(ClassTerm.`properties`)
      propertyTerm <- voc.mapElementWithId(Vocabulary.propertyTerms, property)
    } yield {
      propertyTerm.addValue(PropertyTerm.domain, classTerm.id)
    }
  }
}

object VocabularyLanguageDefinition extends Dialect("RAML 1.0 Vocabulary", "", Vocabulary, resolver = (root: Root) => new BasicResolver(root, List(Vocabulary.externals))){
  refiner = {
    val ref = new VocabularyRefiner()
    Some(ref)
  }
}