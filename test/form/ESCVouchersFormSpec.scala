/*
 * Copyright 2016 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package form

import controllers.FakeCCApplication
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.test.UnitSpec

class ESCVouchersFormSpec extends UnitSpec with FakeCCApplication with MockitoSugar {

    "ESCVouchersFormSpec" should {

      "accept valid selection - Yes"  in {
        new ESCVouchersFormInstance(parent = true).form.bind(Map(
          "doYouGetVouchers" -> "Yes"
        )).fold(
          errors =>
            errors.errors shouldBe empty,
          success =>
            success shouldBe Some("Yes")
        )

      }

      "accept valid selection - No"  in {
        new ESCVouchersFormInstance(parent = true).form.bind(Map(
          "doYouGetVouchers" -> "No"
        )).fold(
          errors =>
            errors.errors shouldBe empty,
          success =>
            success shouldBe Some("No")
        )

      }

      "accept valid selection - Not sure"  in {
        new ESCVouchersFormInstance(parent = true).form.bind(Map(
          "doYouGetVouchers" -> "notSure"
        )).fold(
          errors =>
            errors.errors shouldBe empty,
          success =>
            success shouldBe Some("notSure")
        )

      }
      "throw validation error when nothing is selected for partner" in {
        new ESCVouchersFormInstance(parent = false).form.bind(Map(
          "doYouGetVouchers" -> ""
        )).fold(
          errors =>
            errors.errors.head.message shouldBe "You must select Yes, No or Not sure",
          success =>
            success should not be true
        )
      }

      "throw validation error when nothing is selected for parent" in {
        new ESCVouchersFormInstance(parent = true).form.bind(Map(
          "doYouGetVouchers" -> ""
        )).fold(
          errors =>
            errors.errors.head.message shouldBe "You must select Yes, No or Not sure",
          success =>
            success should not be true
        )
      }

      "prepopulate the form with a value" in {
        val form = new ESCVouchersFormInstance().form.fill(Some("Yes"))
        form.get shouldBe Some("Yes")
      }
    }


}
