package amf.dialects
import amf.AmlProfile
import amf.client.parse.DefaultParserErrorHandler
import amf.core.errorhandling.AmfStaticReportBuilder
import amf.core.unsafe.PlatformSecrets
import amf.core.validation.AMFValidationReport
import amf.core.{AMFCompiler, CompilerContextBuilder}
import amf.facades.Validation
import amf.io.FileAssertionTest
import amf.plugins.document.vocabularies.AMLPlugin
import amf.plugins.features.validation.AMFValidatorPlugin
import amf.plugins.features.validation.emitters.ValidationReportJSONLDEmitter
import org.scalatest.{Assertion, AsyncFunSuite, Matchers}

import scala.concurrent.{ExecutionContext, Future}

trait DialectDefinitionValidationTest extends AsyncFunSuite with Matchers with FileAssertionTest with PlatformSecrets {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

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

  test("Test dialect with enums for literal mappings") {
    validate("/enums/dialect.yaml")
  }

  private val path: String = "amf-client/shared/src/test/resources/vocabularies2/instances/invalids"

  protected def validate(dialect: String, goldenReport: Option[String] = None): Future[Assertion] = {
    // Static initialize
    amf.core.AMF.registerPlugin(AMLPlugin)
    amf.core.AMF.registerPlugin(AMFValidatorPlugin)

    // Validate
    buildParsingReportFor(dialect).flatMap { actualReport =>
      goldenReport match {
        case Some(r) =>
          writeTemporaryFile(path + r)(ValidationReportJSONLDEmitter.emitJSON(actualReport))
            .flatMap(assertDifferences(_, path + r))
        case None =>
          if (!actualReport.conforms) {
            println(actualReport.toString)
          }
          actualReport.conforms should be(true)
      }
    }
  }

  /**
    * Dialects do not need model validation, model validation is only performed on dialect instances. Dialect definition
    * errors are reported during parsing
    * @param dialect dialect path relative to $path
    * @return
    */
  private def buildParsingReportFor(dialect: String): Future[AMFValidationReport] = {
    for {
      _ <- Validation(platform)
      ctx <- {
        val path               = "file://" + this.path + dialect
        val parserErrorHandler = DefaultParserErrorHandler.withRun()
        val builder            = new CompilerContextBuilder(path, platform, eh = parserErrorHandler)
        Future.successful(builder.build())
      }
      dialect <- {
        new AMFCompiler(
          ctx,
          Some("application/yaml"),
          Some(AMLPlugin.ID)
        ).build()
      }
    } yield {
      val parsingReport = new AmfStaticReportBuilder(dialect, AmlProfile)
      parsingReport.buildFromStatic()
    }
  }
}
