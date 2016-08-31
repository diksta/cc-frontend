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

/**
 * Created by user on 11/04/16.
 */
class DoYouLiveWithPartnerFormSpec extends UnitSpec with FakeCCApplication with MockitoSugar {

  "DoYouLiveWithPartnerForm" should {

    "accept when true is selected" in {
      DoYouLiveWithPartnerForm.form.bind(Map(
        "doYouLiveWithYourPartner" -> "true"
      )).fold(
          errors =>
            errors.errors shouldBe empty,
          success =>
            success shouldBe Some(true)
        )
    }

    "accept when false is selected" in {
      DoYouLiveWithPartnerForm.form.bind(Map(
        "doYouLiveWithYourPartner" -> "false"
      )).fold(
          errors =>
            errors.errors shouldBe empty,
          success =>
            success shouldBe Some(false)
        )
    }

    "throw validation error when nothing is selected" in {
      DoYouLiveWithPartnerForm.form.bind(Map(
        "doYouLiveWithYourPartner" -> ""
      )).fold(
          errors =>
            errors.errors.head.message shouldBe "You must select Yes or No",
          success =>
            success should not be Some(true)
        )
    }

    "prepopulate the form with a value" in {
      val form = DoYouLiveWithPartnerForm.form.fill(Some(true))
      form.get shouldBe Some(true)
    }

  }

}
