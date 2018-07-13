package amf.error
import amf.core.model.document.BaseUnit
import amf.core.parser.Range
import amf.core.remote.OasYamlHint
import amf.facades.{AMFCompiler, Validation}

import scala.concurrent.Future

class OasParserErrorTest extends ParserErrorTest {

  test("Empty type ref") {
    validate(
      "/error/empty-type-ref.yaml",
      violation => {
        violation.level should be("Violation")
        violation.message should be("Unexpected $ref with $ref: ")
        violation.position.map(_.range) should be(Some(Range((13, 9), (13, 9))))
      },
      refViolation => {
        refViolation.level should be("Violation")
        refViolation.message should be("Error parsing shape at NewSchema")
        refViolation.position.map(_.range) should be(Some(Range((12, 2), (13, 9))))
      }
    )
  }

  test("Invalid parameter binding") {
    validate(
      "/error/invalid-parameter-binding.json",
      violation => {
        violation.level should be("Violation")
        violation.message should be("Invalid parameter binding 'bo'")
        violation.position.map(_.range) should be(Some(Range((13, 18), (13, 22))))
      },
      refViolation => {
        refViolation.level should be("Violation")
        refViolation.message should be("Invalid parameter binding 'qu'")
        refViolation.position.map(_.range) should be(Some(Range((25, 18), (25, 22))))
      }
    )
  }

  test("Invalid body parameter count") {
    validate(
      "/error/invalid-body-parameter.json",
      violation => {
        violation.level should be("Violation")
        violation.message should be("Cannot declare more than one body parameter for a request")
        violation.position.map(_.range) should be(Some(Range((22, 13), (33, 11))))
      }
    )
  }

  override protected val basePath: String = "file://amf-client/shared/src/test/resources/parser-results/oas"

  override protected def build(validation: Validation, file: String): Future[BaseUnit] =
    AMFCompiler(file, platform, OasYamlHint, validation).build()
}
