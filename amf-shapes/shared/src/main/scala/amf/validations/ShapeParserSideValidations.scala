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

  override val levels: Map[String, Map[ProfileName, String]] = Map(
    ExclusiveLinkTargetError.id -> all(VIOLATION),
    OasBodyAndFormDataParameterSpecification.id -> Map(
      OasProfile   -> VIOLATION,
      Oas20Profile -> VIOLATION
    ),
    OasInvalidBodyParameter.id    -> all(VIOLATION),
    OasInvalidParameterBinding.id -> all(VIOLATION),
    OasFormDataNotFileSpecification.id -> Map(
      OasProfile   -> VIOLATION,
      Oas20Profile -> VIOLATION
    ),
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
    ItemsFieldRequiredWarning.id                 -> all(WARNING), // TODO: should be violation
    NullAbstractDeclaration.id                   -> all(WARNING),
    SchemaDeprecated.id                          -> all(WARNING),
    SchemasDeprecated.id                         -> all(WARNING),
    UnusedBaseUriParameter.id                    -> all(WARNING),
    InvalidShapeFormat.id                        -> all(WARNING),
    CrossSecurityWarningSpecification.id         -> all(WARNING),
    ReadOnlyPropertyMarkedRequired.id            -> all(WARNING),
    MissingRequiredFieldForGrantType.id          -> all(WARNING),
    invalidExampleFieldWarning.id                -> all(WARNING), // TODO: should be violation
    OasInvalidParameterSchema.id                 -> all(WARNING), // TODO: should be violation
    InvalidAllowedTargets.id                     -> all(WARNING), // TODO: should be violation
    MissingDiscriminatorProperty.id              -> all(VIOLATION),
    InvalidPayload.id                            -> all(VIOLATION),
    ClosedShapeSpecificationWarning.id           -> all(WARNING),
    ImplicitVersionParameterWithoutApiVersion.id -> all(WARNING), // TODO: should be violation
    InvalidVersionBaseUriParameterDefinition.id  -> all(WARNING), // TODO: should be violation
    HeaderMustBeObject.id                        -> Map(Async20Profile -> VIOLATION),
    InvalidRequiredBooleanForSchemaVersion.id    -> all(WARNING) // TODO: should be violation
  )

  override val validations: List[ValidationSpecification] = List(
    InvalidJsonSchemaType,
  )
}
