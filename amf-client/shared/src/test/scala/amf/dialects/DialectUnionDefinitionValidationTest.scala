package amf.dialects
import org.mulesoft.common.io.Fs

class DialectUnionDefinitionValidationTest extends DialectDefinitionValidationTest {

  override protected val path: String = "amf-client/shared/src/test/resources/vocabularies2/instances/invalids/unions/"

  protected val dirs: Array[String] = Fs.syncFile(path).list

  dirs.foreach { `case` =>
    test(s"Validate dialect definition for ${`case`}") {
      val dialect = s"${`case`}/dialect.yaml"
      val report  = s"${`case`}/report.json"
      validate(dialect, Some(report))
    }
  }

}
