package amf.error
import amf.core.errorhandling.AMFErrorHandler
import amf.core.model.document.BaseUnit
import amf.core.parser.Range
import amf.core.remote.Oas20YamlHint
import amf.facades.AMFCompiler

import scala.concurrent.Future

class OasParserErrorTest extends ParserErrorTest {

  test("Empty type ref") {
    validate(
      "/error/empty-type-ref.yaml",
      violation => {
        violation.severityLevel should be("Violation")
        violation.message should be("Unexpected $ref with $ref: ")
        violation.position.map(_.range) should be(Some(Range((14, 9), (14, 9))))
      }
    )
  }

  test("Invalid parameter binding") {
    validate(
      "/error/invalid-parameter-binding.json",
      violation => {
        violation.severityLevel should be("Violation")
        violation.message should be("Invalid parameter binding 'bo'")
        violation.position.map(_.range) should be(Some(Range((13, 18), (13, 22))))
      },
      refViolation => {
        refViolation.severityLevel should be("Violation")
        refViolation.message should be("Invalid parameter binding 'qu'")
        refViolation.position.map(_.range) should be(Some(Range((25, 18), (25, 22))))
      }
    )
  }

  test("Invalid body parameter count") {
    validate(
      "/error/invalid-body-parameter.json",
      violation => {
        violation.severityLevel should be("Violation")
        violation.message should be("Cannot declare more than one 'body' parameter for a request or a resource")
        violation.position.map(_.range) should be(Some(Range((22, 13), (33, 11))))
      }
    )
  }

  override protected val basePath: String = "file://amf-client/shared/src/test/resources/parser-results/oas"

  override protected def build(eh: AMFErrorHandler, file: String): Future[BaseUnit] =
    AMFCompiler(file, platform, Oas20YamlHint, eh = eh).build()
}
