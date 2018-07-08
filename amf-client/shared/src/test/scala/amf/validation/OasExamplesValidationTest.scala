package amf.validation

import amf.core.remote.{Hint, OasJsonHint}

class OasExamplesValidationTest extends ValidationReportGenTest {

  override val basePath: String    = "file://amf-client/shared/src/test/resources/validations/"
  override val reportsPath: String = "amf-client/shared/src/test/resources/validations/reports/examples/"

  test("Test examples in oas") {
    validatePlatform("/examples/examples-in-oas.json", Some("examples-in-oas.report"))
  }

  override val hint: Hint = OasJsonHint
}
