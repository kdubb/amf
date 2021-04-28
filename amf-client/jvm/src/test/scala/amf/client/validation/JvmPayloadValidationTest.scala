package amf.client.validation

import amf.client.model.DataTypes
import amf.client.model.domain.shapes.ScalarShape
import amf.convert.NativeOpsFromJvm
import amf.core.AMF
import amf.plugins.document.webapi.validation.PayloadValidatorPlugin

class JvmPayloadValidationTest extends ClientPayloadValidationTest with NativeOpsFromJvm {
  // TODO: Shapes - REMOD

  //  test("Test unexpected type error") {
//    AMF.init().flatMap { _ =>
//      amf.Core.registerPlugin(PayloadValidatorPlugin)
//
//      val test = new ScalarShape().withDataType(DataTypes.String)
//
//      val report = test
//        .payloadValidator("application/json")
//        .asOption
//        .get
//        .syncValidate("application/json", "1234")
//      report.conforms shouldBe false
//      report.results.asSeq.head.message shouldBe "expected type: String, found: Integer" // APIKit compatibility
//    }
//  }
}
