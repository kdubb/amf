package amf.compiler

import amf.Raml10Profile
import amf.client.environment.RAMLConfiguration
import amf.client.parse.DefaultErrorHandler
import amf.client.remod.{AMFGraphConfiguration, AMFValidator}
import amf.client.remod.amfcore.plugins.validate.ValidationConfiguration
import amf.core.Root
import amf.core.errorhandling.UnhandledErrorHandler
import amf.core.model.document.{BaseUnit, Document}
import amf.core.parser.{UnspecifiedReference, _}
import amf.core.remote.Syntax.{Syntax, Yaml}
import amf.core.remote._
import amf.facades.Validation
import amf.plugins.domain.webapi.models.api.WebApi
import org.scalatest.Matchers._
import org.scalatest.{Assertion, AsyncFunSuite}
import org.yaml.model.{YMap, YMapEntry}

import scala.concurrent.ExecutionContext

class AMFCompilerTest extends AsyncFunSuite with CompilerTestBuilder {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  test("Api (raml)") {
    build("file://amf-client/shared/src/test/resources/tck/raml-1.0/Api/test003/api.raml", Raml10YamlHint) map assertDocument
  }

  test("Vocabulary") {
    build("file://amf-client/shared/src/test/resources/vocabularies2/production/raml_doc.yaml", VocabularyYamlHint) map {
      _ should not be null
    }
  }

  test("Api (oas)") {
    build("file://amf-client/shared/src/test/resources/tck/raml-1.0/Api/test003/api.openapi", Oas20JsonHint) map assertDocument
  }

  test("Api (amf)") {
    build("file://amf-client/shared/src/test/resources/tck/raml-1.0/Api/test003/api.jsonld", AmfJsonHint) map assertDocument
  }

  test("Simple import") {
    build("file://amf-client/shared/src/test/resources/input.json", Oas20JsonHint) map {
      _ should not be null
    }
  }

  test("Reference in imports with cycles (yaml)") {
    assertCycles(Yaml, Raml10YamlHint)
  }

  test("Simple cicle (yaml)") {
    recoverToExceptionIf[Exception] {
      Validation(platform)
        .flatMap(
          v =>
            build(s"file://amf-client/shared/src/test/resources/reference-itself.raml",
                  Raml10YamlHint,
                  validation = Some(v),
                  eh = Some(UnhandledErrorHandler)))
    } map { ex =>
      assert(ex.getMessage.contains(
        s"Cyclic found following references file://amf-client/shared/src/test/resources/reference-itself.raml -> file://amf-client/shared/src/test/resources/reference-itself.raml"))
    }
  }

  test("Cache duplicate imports") {
    val cache = new TestCache()
    build("file://amf-client/shared/src/test/resources/input-duplicate-includes.json",
          Oas20JsonHint,
          cache = Some(cache)) map { _ =>
      cache.assertCacheSize(2)
    }
  }

  test("Cache different imports") {
    val cache = new TestCache()
    build("file://amf-client/shared/src/test/resources/input.json", Oas20JsonHint, cache = Some(cache)) map { _ =>
      cache.assertCacheSize(3)
    }
  }

  test("Libraries (raml)") {
    compiler("file://amf-client/shared/src/test/resources/modules.raml", Raml10YamlHint)
      .flatMap(_.root()) map {
      case Root(root, _, _, references, UnspecifiedReference, _) =>
        val body = root.asInstanceOf[SyamlParsedDocument].document.as[YMap]
        body.entries.size should be(2)
        assertUses(body.key("uses").get, references.map(_.unit))
      case Root(root, _, _, refKind, _, _) => throw new Exception(s"Unespected type of referenceKind parsed $refKind")
    }
  }

  test("Libraries (oas)") {
    compiler("file://amf-client/shared/src/test/resources/modules.json", Oas20JsonHint)
      .flatMap(_.root()) map {
      case Root(root, _, _, references, UnspecifiedReference, _) =>
        val body = root.asInstanceOf[SyamlParsedDocument].document.as[YMap]
        body.entries.size should be(3)
        assertUses(body.key("x-amf-uses").get, references.map(_.unit))
      case Root(root, _, _, refKind, _, _) => throw new Exception(s"Unespected type of referenceKind parsed $refKind")
    }
  }

  test("Non existing included file") {
    val eh = DefaultErrorHandler()
    Validation(platform)
      .flatMap(v => {

        build("file://amf-client/shared/src/test/resources/non-exists-include.raml",
              Raml10YamlHint,
              validation = Some(v),
              eh = Some(eh))
          .flatMap(bu => {
            AMFValidator.validate(bu, Raml10Profile, RAMLConfiguration.RAML10().withErrorHandlerProvider(() => eh))
          })
      })
      .map(r => {
        assert(!r.conforms)
        assert(r.results.lengthCompare(2) == 0)
        assert(
          r.results.last.message
            .contains("amf-client/shared/src/test/resources/nonExists.raml"))
        assert(
          r.results.last.message
            .contains("such file or directory")) // temp, assert better the message for js and jvm
      })
  }

  private def assertDocument(unit: BaseUnit): Assertion = unit match {
    case d: Document =>
      d.encodes.asInstanceOf[WebApi].servers.headOption.map(_.url.value()).getOrElse("") should be("api.example.com")
      d.encodes.asInstanceOf[WebApi].name.value() should be("test")
  }

  private def assertUses(uses: YMapEntry, references: Seq[BaseUnit]) = {
    uses.key.as[String] should include("uses")

    val libraries = uses.value.as[YMap]

    libraries.map.values.foreach(value => {
      val s: String = value
      s should include("libraries")
    })

    libraries.entries.length should be(references.size)
  }

  private def assertCycles(syntax: Syntax, hint: Hint) = {
    recoverToExceptionIf[Exception] {
      Validation(platform)
        .flatMap(v => {
          build(s"file://amf-client/shared/src/test/resources/input-cycle.${syntax.extension}",
                hint,
                validation = Some(v),
                eh = Some(UnhandledErrorHandler))
        })
    } map { ex =>
      assert(ex.getMessage.contains(
        s"Cyclic found following references file://amf-client/shared/src/test/resources/input-cycle.${syntax.extension} -> file://amf-client/shared/src/test/resources/includes/include-cycle.${syntax.extension} -> file://amf-client/shared/src/test/resources/input-cycle.${syntax.extension}"))
    }
  }

  private class TestCache extends Cache {
    def assertCacheSize(expectedSize: Int): Assertion = {
      if (size != expectedSize) {
        cache.foreach {
          case (a, b) =>
            println(s"$a -> ${System.identityHashCode(b)}")
        }
      }
      size should be(expectedSize)
    }
  }
}
