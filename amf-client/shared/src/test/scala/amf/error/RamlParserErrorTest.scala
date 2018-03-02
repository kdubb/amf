package amf.error

import amf.compiler.CompilerTestBuilder
import amf.core.parser.Range
import amf.core.remote.RamlYamlHint
import amf.core.validation.AMFValidationResult
import amf.facades.{AMFCompiler, Validation}
import amf.plugins.features.validation.ParserSideValidations
import org.scalatest.Matchers._
import org.scalatest.{AsyncFunSuite, Succeeded}

import scala.concurrent.ExecutionContext

class RamlParserErrorTest extends AsyncFunSuite with CompilerTestBuilder {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  private val basePath = "file://amf-client/shared/src/test/resources/error/"

  test("Test unexpected node types") {
    validate(
      "unexpected-nodes.raml",
      invalid => {
        invalid.level should be("Violation")
        invalid.message should be("Unexpected key 'invalid'. Options are 'value' or annotations \\(.+\\)")
        invalid.position.map(_.range) should be(Some(Range((3, 4), (3, 11))))
      },
      title => {
        title.level should be("Violation")
        title.message should be("Expecting !!str and !!null provided")
      },
      description => {
        description.level should be("Violation")
        description.message should be("Expecting !!str and !!seq provided")
        description.position.map(_.range) should be(Some(Range((4, 13), (4, 24))))
      },
      protocols => {
        protocols.level should be("Violation")
        protocols.message should be("WebAPI 'protocols' property must be a scalar or sequence value")
        protocols.position.map(_.range) should be(Some(Range((5, 10), (7, 0))))
      },
      securedBy => {
        securedBy.level should be("Violation")
        securedBy.message should be("Not a YSequence")
        securedBy.position.map(_.range) should be(Some(Range((7, 11), (7, 16))))
      }
    )
  }

  test("Custom facets work correctly with the closed node detection mechanism") {
    validate(
      "custom-facets.raml",
      erroneousTypeShape => {
        erroneousTypeShape.level should be("Violation")
        erroneousTypeShape.targetNode should be(
          "file://amf-client/shared/src/test/resources/error/custom-facets.raml#/declarations/scalar/ErroneousType")
        erroneousTypeShape.validationId should be(ParserSideValidations.ClosedShapeSpecification.id())
      },
      incorrect1 => {
        incorrect1.level should be("Violation")
        incorrect1.targetNode should be(
          "file://amf-client/shared/src/test/resources/error/custom-facets.raml#/declarations/union/Incorrect1")
        incorrect1.validationId should be(ParserSideValidations.ClosedShapeSpecification.id())
      },
      incorrect2 => {
        incorrect2.level should be("Violation")
        incorrect2.targetNode should be(
          "file://amf-client/shared/src/test/resources/error/custom-facets.raml#/declarations/union/Incorrect2")
        incorrect2.validationId should be(ParserSideValidations.ClosedShapeSpecification.id())
      },
      incorrect3 => {
        incorrect3.level should be("Violation")
        incorrect3.targetNode should be(
          "file://amf-client/shared/src/test/resources/error/custom-facets.raml#/declarations/union/Incorrect3")
        incorrect3.validationId should be(ParserSideValidations.ClosedShapeSpecification.id())
      }
    )
  }

  test("Invalid node parsing type") {
    validate(
      "invalid-type.raml",
      artist => {
        artist.level should be("Violation")
        artist.message should be("Expecting !!str and !!seq provided")
        artist.position.map(_.range) should be(Some(Range((115, 10), (115, 12))))
      },
      tracks => {
        tracks.level should be("Violation")
        tracks.message should be("Expecting !!str and !!seq provided")
        tracks.position.map(_.range) should be(Some(Range((120, 10), (120, 12))))
      }
    )
  }

  private def validate(file: String, fixture: (AMFValidationResult => Unit)*) = {
    Validation(platform).flatMap { validation =>
      AMFCompiler(basePath + file, platform, RamlYamlHint, validation)
        .build()
        .map { _ =>
          val report = validation.aggregatedReport
          if (report.size != fixture.size) report.foreach(println)
          report.size should be(fixture.size)
          fixture.zip(report).foreach {
            case (fn, result) => fn(result)
          }
          Succeeded
        }
    }
  }
}
