package amf.dialects

trait SimpleDialectDefinitionValidationTest extends DialectDefinitionValidationTest {

  test("Test missing version") {
    validate("/missing-version/dialect.yaml", Some("/missing-version/report.json"))
  }

  test("Test missing dialect name") {
    validate("/missing-dialect-name/dialect.yaml", Some("/missing-dialect-name/report.json"))
  }

  test("Test invalid property term uri for description") {
    validate("/schema-uri/dialect.yaml", Some("/schema-uri/report.json"))
  }

  test("Test missing range in property mapping") {
    validate("/missing-range-in-mapping/dialect.yaml", Some("/missing-range-in-mapping/report.json"))
  }

  override protected val path: String = "amf-client/shared/src/test/resources/vocabularies2/instances/invalids"
}
