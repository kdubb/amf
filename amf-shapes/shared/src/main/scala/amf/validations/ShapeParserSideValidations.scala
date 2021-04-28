package amf.validations

import amf.core.validation.SeverityLevels.{VIOLATION, WARNING}
import amf.core.validation.core.ValidationSpecification
import amf.{AmfProfile, Async20Profile, Oas20Profile, Oas30Profile, OasProfile, ProfileName, Raml08Profile, Raml10Profile, RamlProfile}
import amf.core.validation.core.ValidationSpecification.PARSER_SIDE_VALIDATION
import amf.core.vocabulary.Namespace
import amf.core.vocabulary.Namespace.AmfParser
import amf.plugins.features.validation.Validations

object ShapeParserSideValidations extends Validations {

  override val specification: String = PARSER_SIDE_VALIDATION
  override val namespace: Namespace  = AmfParser

  val InvalidJsonSchemaType = validation(
    "invalid-json-schema-type",
    "Invalid json schema definition type"
  )

  val InvalidDisjointUnionType = validation(
    "invalid-disjoint-union-type",
    "Invalid type for disjoint union"
  )

  val InvalidXoneType = validation(
    "invalid-xone-type",
    "Xone should be a sequence"
  )

  val InvalidAndType = validation(
    "invalid-and-type",
    "And should be a sequence"
  )

  val InvalidOrType = validation(
    "invalid-or-type",
    "Or should be a sequence"
  )

  val InvalidUnionType = validation(
    "invalid-union-type",
    "Union should be a sequence"
  )

  val ItemsFieldRequired = validation(
    "items-field-required",
    "'items' field is required when type is array"
  )

  // TODO: Should be removed and used the violation in the next major
  val ItemsFieldRequiredWarning = validation(
    "items-field-required-warning",
    "'items' field is required when type is array"
  )

  val InvalidAdditionalItemsType = validation(
    "invalid-additional-items-type",
    "additionalItems should be a boolean or a map"
  )

  val InvalidAdditionalPropertiesType = validation(
    "invalid-additional-properties-type",
    "additionalProperties should be a boolean or a map"
  )

  val InvalidUnevaluatedPropertiesType = validation(
    "invalid-unevaluated-properties-type",
    "unevaluatedProperties should be a boolean or a map"
  )

  val InvalidUnevaluatedItemsType = validation(
    "invalid-unevaluated-items-type",
    "unevaluatedItems should be a boolean or a map"
  )

  val InvalidTupleType = validation(
    "invalid-tuple-type",
    "Tuple should be a sequence"
  )

  val InvalidSchemaType = validation(
    "invalid-schema-type",
    "Schema should be a string"
  )

  val InvalidRequiredValue = validation(
    "invalid-required-value",
    "Invalid required value"
  )

  val InvalidRequiredArrayForSchemaVersion = validation(
    "invalid-required-array-for-schema-version",
    "Required arrays of properties not supported in JSON Schema below version draft-4"
  )

  val InvalidRequiredBooleanForSchemaVersion = validation(
    "invalid-required-boolean-for-schema-version",
    "Required property boolean value is only supported in JSON Schema draft-3"
  )

  val DuplicateRequiredItem = validation(
    "duplicate-required-item",
    "Duplicate required item"
  )

  val DiscriminatorNameRequired = validation(
    "discriminator-name-required",
    "Discriminator property name is required"
  )

  val ReadOnlyPropertyMarkedRequired = validation(
    "read-only-property-marked-required",
    "Read only property should not be marked as required by a schema"
  )

  val InvalidMediaTypeType = validation(
    "invalid-media-type-type",
    "Media type should be a string"
  )

  val InvalidAnnotationTarget = validation(
    "invalid-annotation-target",
    "Annotation not allowed in used target"
  )

  val ExceededMaxYamlReferences = validation(
    "max-yaml-references",
    "Exceeded maximum yaml references threshold"
  )

  val ExclusivePropertiesSpecification = validation(
    "exclusive-properties-error",
    "Exclusive properties declared together"
  )

  val ExamplesMustBeAMap = validation(
    "examples-must-be-map",
    "Examples value should be a map"
  )

  val InvalidShapeFormat = validation(
    "invalid-shape-format",
    "Invalid shape format"
  )

  val UnexpectedVendor = validation(
    "unexpected-vendor",
    "Unexpected vendor"
  )

  val UnexpectedRamlScalarKey = validation(
    "unexpected-raml-scalar-key",
    "Unexpected key. Options are 'value' or annotations \\(.+\\)"
  )

  val DuplicatePropertySpecification = validation(
    "duplicated-property",
    "Duplicated property in node"
  )

  val InvalidFragmentType = validation(
    "invalid-fragment-type",
    "Invalid fragment type"
  )

  val MissingRequiredUserDefinedFacet = validation(
    "missing-user-defined-facet",
    "Type is missing required user defined facet"
  )

  val UserDefinedFacetMatchesBuiltInFacets = validation(
    "user-defined-facets-matches-built-in",
    "User defined facet name matches built in facet of type"
  )

  val UserDefinedFacetMatchesAncestorsTypeFacets = validation(
    "user-defined-facets-matches-ancestor",
    "User defined facet name matches ancestor type facet"
  )

  val UnableToParseShapeExtensions = validation(
    "unable-to-parse-shape-extensions",
    "Unable to parse shape extensions"
  )

  val ChainedReferenceSpecification = validation(
    "chained-reference-error",
    "References cannot be chained"
  )

  val UnableToSetDefaultType = validation(
    "unable-to-set-default-type",
    "Unable to set default type"
  )

  val InvalidTypeDefinition = validation(
    "invalid-type-definition",
    "Invalid type definition"
  )

  val InvalidDatetimeFormat = validation(
    "invalid-datetime-format",
    "Invalid format value for datetime"
  )

  val InvalidDecimalPoint = validation(
    "invalid-decimal-point",
    "Invalid decimal point"
  )

  val UnexpectedFileTypesSyntax = validation(
    "unexpected-file-types-syntax",
    "Unexpected 'fileTypes' syntax. Options are string or sequence"
  )

  val UnableToParseArray = validation(
    "unable-to-parse-array",
    "Unable to parse array definition"
  )

  val JsonSchemaInheritanceWarning = validation(
    "json-schema-inheritance",
    "Inheriting from JSON Schema"
  )

  val XmlSchemaInheritancceWarning = validation(
    "xml-schema-inheritance",
    "Inheriting from XML Schema"
  )

  val PatternPropertiesOnClosedNodeSpecification = validation(
    "pattern-properties-on-closed-node",
    "Closed node cannot define pattern properties"
  )

  val MissingDiscriminatorProperty = validation(
    "missing-discriminator-property",
    "Type is missing property marked as discriminator"
  )

  val InvalidValueInPropertiesFacet = validation(
    "invalid-value-in-properties-facet",
    "Properties facet must be a map of key and values"
  )

  val DiscriminatorOnExtendedUnionSpecification = validation(
    "discriminator-on-extended-union",
    "Property 'discriminator' not supported in a node extending a unionShape"
  )

  val InvalidPropertyType = validation(
    "invalid-property-type",
    "Invalid property key type. Should be string"
  )

  val InvalidXmlSchemaType = validation(
    "invalid-xml-schema-type",
    "Invalid xml schema type"
  )

  val SchemaDeprecated = validation(
    "schema-deprecated",
    "'schema' keyword it's deprecated for 1.0 version, should use 'type' instead"
  )

  val SchemasDeprecated = validation(
    "schemas-deprecated",
    "'schemas' keyword it's deprecated for 1.0 version, should use 'types' instead"
  )

  val ExclusiveSchemaType = validation(
    "exclusive-schema-type",
    "'schema' and 'type' properties are mutually exclusive"
  )

  val ExclusiveSchemasType = validation(
    "exclusive-schemas-type",
    "'schemas' and 'types' properties are mutually exclusive"
  )

  val JsonSchemaFragmentNotFound = validation(
    "json-schema-fragment-not-found",
    "Json schema fragment not found"
  )

  val UnableToParseJsonSchema = validation(
    "unable-to-parse-json-schema",
    "Unable to parse json schema"
  )

  val InvalidJsonSchemaVersion = validation(
    "invalid-json-schema-version",
    "Invalid Json Schema version"
  )

  val InvalidAbstractDeclarationParameterInType = validation(
    "invalid-abstract-declaration-parameter-in-type",
    "Trait/Resource Type parameter in type"
  )

  val InvalidExternalTypeType = validation(
    "invalid-external-type-type",
    "Invalid external type type"
  )

  val InvalidTypeExpression = validation(
    "invalid-type-expression",
    "Invalid type expression"
  )


  override val levels: Map[String, Map[ProfileName, String]] = Map(
    InvalidRequiredBooleanForSchemaVersion.id    -> all(WARNING), // TODO: should be violation
      ReadOnlyPropertyMarkedRequired.id            -> all(WARNING),
    InvalidShapeFormat.id                        -> all(WARNING),
    ItemsFieldRequiredWarning.id                 -> all(WARNING), // TODO: should be violation
    SchemaDeprecated.id                          -> all(WARNING),
    SchemasDeprecated.id                         -> all(WARNING),
    MissingDiscriminatorProperty.id              -> all(VIOLATION),
    JsonSchemaInheritanceWarning.id -> all(WARNING),
    PatternPropertiesOnClosedNodeSpecification.id -> Map(
      RamlProfile   -> VIOLATION,
      Raml10Profile -> VIOLATION,
      Raml08Profile -> VIOLATION,
      OasProfile    -> WARNING,
      Oas20Profile  -> WARNING,
      Oas30Profile  -> WARNING,
      AmfProfile    -> WARNING
    ),
    DiscriminatorOnExtendedUnionSpecification.id -> Map(
      RamlProfile   -> VIOLATION,
      Raml10Profile -> VIOLATION,
      Raml08Profile -> VIOLATION,
      OasProfile    -> WARNING,
      Oas20Profile  -> WARNING,
      Oas30Profile  -> WARNING,
      AmfProfile    -> WARNING
    ),
  )

  override val validations: List[ValidationSpecification] = List(
    InvalidJsonSchemaType,
  )
}
