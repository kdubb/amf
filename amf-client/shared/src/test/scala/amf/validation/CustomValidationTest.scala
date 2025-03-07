package amf.validation
import amf.{ProfileName, RAMLStyle}
import amf.core.remote.{Hint, RamlYamlHint}

class CustomValidationTest extends UniquePlatformReportGenTest {

  override val basePath    = "file://amf-client/shared/src/test/resources/validations/"
  override val reportsPath = "amf-client/shared/src/test/resources/validations/reports/"
  override val hint: Hint  = RamlYamlHint

  test("Load dialect") {
    validate("mercury.raml",
             Some(s"mercury1.report.${platform.name}"),
             ProfileName("mercury"),
             Some("profiles/mercury.yaml"))
  }

  test("Additional raml validations") {
    validate("amf_extended.raml",
             Some("amf_extended.report"),
             ProfileName("amf_extended", RAMLStyle),
             Some("profiles/amf_extended.yaml"))
  }

  test("Property pairs validations") {
    validate("pairs.raml", Some("pairs.report"), ProfileName("pairs"), Some("profiles/pairs.yaml"))
  }

  test("Paths validations") {
    validate("paths.raml", Some("paths.report"), ProfileName("paths"), Some("profiles/paths.yaml"))
  }

  test("Paths validations 2") {
    validate("paths2.raml", Some("paths2.report"), ProfileName("paths2"), Some("profiles/paths2.yaml"))
  }

  test("Logical constraints") {
    validate("logical.raml", Some("logical.report"), ProfileName("logical"), Some("profiles/logical.yaml"))
  }

  test("Defining raml validation as warning") {
    validate("banking-api-error.raml",
             Some("banking.report"),
             ProfileName("Banking"),
             Some("profiles/validation-profile.yaml"))
  }

  if (platform.name == "jvm") { // not supported in JS yet
    test("Query constraints") {
      validate("query.raml", Some("query.report"), ProfileName("query"), Some("profiles/query.yaml"))
    }
  }
}
