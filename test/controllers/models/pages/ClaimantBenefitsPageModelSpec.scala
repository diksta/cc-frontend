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

package controllers.models.pages

import controllers.FakeCCApplication
import models.pages.ClaimantBenefitsPageModel
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.test.UnitSpec

/**
 * Created by user on 19/02/16.
 */
class ClaimantBenefitsPageModelSpec extends UnitSpec with FakeCCApplication with MockitoSugar {
  
  "ClaimantBenefitsPageModel" should {

    "return true if they've made a correct selection" in {
      val model = ClaimantBenefitsPageModel(
        incomeBenefit = false,
        disabilityBenefit = false,
        severeDisabilityBenefit = false,
        carerAllowanceBenefit = false,
        noBenefit= true
      )
      model.validSelection shouldBe true
    }

    "return false for incorrect selection" in {
      val model = ClaimantBenefitsPageModel(
        incomeBenefit = false,
        disabilityBenefit = false,
        severeDisabilityBenefit = false,
        carerAllowanceBenefit = true,
        noBenefit= true
      )
      model.validSelection shouldBe false

    }

    "return true for correct selection - incomeBenefit selected" in {
      val model = ClaimantBenefitsPageModel(
        incomeBenefit = true,
        disabilityBenefit = false,
        severeDisabilityBenefit = false,
        carerAllowanceBenefit = false,
        noBenefit= false
      )
      model.validSelection shouldBe true

    }

    "return true for correct selection - incomeBenefit and disabilityBenefit selected" in {
      val model = ClaimantBenefitsPageModel(
        incomeBenefit = true,
        disabilityBenefit = true,
        severeDisabilityBenefit = false,
        carerAllowanceBenefit = false,
        noBenefit= false
      )
      model.validSelection shouldBe true

    }

    "return true for correct selection - incomeBenefit and disabilityBenefit and  severeDisabilityBenefit and carerAllowanceBenefit selected" in {
      val model = ClaimantBenefitsPageModel(
        incomeBenefit = true,
        disabilityBenefit = true,
        severeDisabilityBenefit = true,
        carerAllowanceBenefit = true,
        noBenefit= false
      )
      model.validSelection shouldBe true

    }

    "return true if one of the checkboxes is selected" in {
      val model = ClaimantBenefitsPageModel(
        incomeBenefit = false,
        disabilityBenefit = false,
        severeDisabilityBenefit = false,
        carerAllowanceBenefit = true,
        noBenefit= false
      )
      model.selection shouldBe true
    }
    "return true if all the checkboxes are selected" in {
      val model = ClaimantBenefitsPageModel(
        incomeBenefit = true,
        disabilityBenefit = true,
        severeDisabilityBenefit = true,
        carerAllowanceBenefit = true,
        noBenefit= true
      )
      model.selection shouldBe true
    }

    "return false if none of the checkboxes is selected" in {
      val model = ClaimantBenefitsPageModel(
        incomeBenefit = false,
        disabilityBenefit = false,
        severeDisabilityBenefit = false,
        carerAllowanceBenefit = false,
        noBenefit= false
      )
      model.selection shouldBe false
    }
  }
}
