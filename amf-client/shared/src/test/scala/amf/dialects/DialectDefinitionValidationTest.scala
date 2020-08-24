package amf.dialects
import amf.ProfileName
import amf.client.parse.DefaultParserErrorHandler
import amf.core.errorhandling.AmfStaticReportBuilder
import amf.core.services.RuntimeValidator
import amf.core.unsafe.PlatformSecrets
import amf.core.{AMFCompiler, CompilerContextBuilder}
import amf.facades.Validation
import amf.io.FileAssertionTest
import amf.plugins.document.vocabularies.AMLPlugin
import amf.plugins.document.vocabularies.model.document.{Dialect, DialectInstance}
import amf.plugins.features.validation.AMFValidatorPlugin
import amf.plugins.features.validation.AMFValidatorPlugin.profileForUnit
import amf.plugins.features.validation.emitters.ValidationReportJSONLDEmitter
import org.mulesoft.common.io.{Fs, Id}
import org.scalatest.{Assertion, AsyncFunSuite, Matchers}

import scala.concurrent.{ExecutionContext, Future}

trait DialectDefinitionValidationTest extends AsyncFunSuite with Matchers with FileAssertionTest with PlatformSecrets {
  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  protected val path: String

  protected def validate(dialect: String, goldenReport: Option[String]): Future[Assertion] = {
    amf.core.AMF.registerPlugin(AMLPlugin)
    amf.core.AMF.registerPlugin(AMFValidatorPlugin)
    val report = for {
      _ <- Validation(platform)
      dialect <- {
        new AMFCompiler(
          new CompilerContextBuilder("file://" + path + dialect, platform, eh = DefaultParserErrorHandler.withRun())
            .build(),
          Some("application/yaml"),
          Some(AMLPlugin.ID)
        ).build()
      }
    } yield {
      val profileName = ProfileName(dialect.asInstanceOf[Dialect].nameAndVersion())
      new AmfStaticReportBuilder(dialect, profileName).buildFromStatic()
    }

    report.flatMap { re =>
      goldenReport match {
        case Some(r) =>
          writeTemporaryFile(path + r)(ValidationReportJSONLDEmitter.emitJSON(re))
            .flatMap(assertDifferences(_, path + r))
        case None => re.conforms should be(true)
      }
    }
  }
}
