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
import models.pages.ClaimantBenefitsPageModel
import org.scalatest.mock.MockitoSugar
import play.api.i18n.Messages
import uk.gov.hmrc.play.test.UnitSpec

/**
 * Created by user on 19/02/16.
 */
class ClaimantBenefitsFormSpec extends UnitSpec with FakeCCApplication with MockitoSugar {
  "ClaimantBenefitsForm" should {

    "accept a valid benefit selection" in {

      val claimantBenefitModel = ClaimantBenefitsPageModel(
        incomeBenefit = false,
        disabilityBenefit = false,
        severeDisabilityBenefit = false,
        carerAllowanceBenefit = false,
        noBenefit = true
      )
      (new ClaimantBenefitsFormInstance).form.bind(
        Map(
          "incomeBenefit" -> "false",
          "disabilityBenefit" -> "false",
          "severeDisabilityBenefit" -> "false",
          "carerAllowanceBenefit" -> "false",
          "noBenefit" -> "true"
        )).fold(
          errors =>
            errors.errors shouldBe empty,
          success =>
            success shouldBe claimantBenefitModel
        )
    }

    "accept a valid benefit selection - disabilityBenefit and severeDisabilityBenefit are selected" in {

      val claimantBenefitModel = ClaimantBenefitsPageModel(
        incomeBenefit = false,
        disabilityBenefit = true,
        severeDisabilityBenefit = true,
        carerAllowanceBenefit = false,
        noBenefit = false
      )
      (new ClaimantBenefitsFormInstance).form.bind(
        Map(
          "incomeBenefit" -> "false",
          "disabilityBenefit" -> "true",
          "severeDisabilityBenefit" -> "true",
          "carerAllowanceBenefit" -> "false",
          "noBenefit" -> "false"
        )).fold(
          errors =>
            errors.errors shouldBe empty,
          success =>
            success shouldBe claimantBenefitModel
        )
    }

    "accept a valid benefit selection - incomeBenefit,disabilityBenefit,severeDisabilityBenefit,carerAllowanceBenefit are selected" in {

      val claimantBenefitModel = ClaimantBenefitsPageModel(
        incomeBenefit = true,
        disabilityBenefit = true,
        severeDisabilityBenefit = true,
        carerAllowanceBenefit = true,
        noBenefit = false
      )
      (new ClaimantBenefitsFormInstance).form.bind(
        Map(
          "incomeBenefit" -> "true",
          "disabilityBenefit" -> "true",
          "severeDisabilityBenefit" -> "true",
          "carerAllowanceBenefit" -> "true",
          "noBenefit" -> "false"
        )).fold(
          errors =>
            errors.errors shouldBe empty,
          success =>
            success shouldBe claimantBenefitModel
        )
    }

    "throw a ValidationError for invalid benefit selection - incomeBenefit and noBenefit are selected (Partner)" in  {

      new ClaimantBenefitsFormInstance(parent = false).form.bind(
        Map(
          "incomeBenefit" -> "true",
          "disabilityBenefit" -> "false",
          "severeDisabilityBenefit" -> "false",
          "carerAllowanceBenefit" -> "false",
          "noBenefit" -> "true"
        )).fold(
          errors =>
            errors.errors.head.message shouldBe Messages("cc.claimant.benefit.invalid.partner.benefits.selected"),
          success =>
            success shouldBe true
        )
    }

    "throw a ValidationError when no benefit is selected" in  {
      (new ClaimantBenefitsFormInstance).form.bind(
        Map(
          "incomeBenefit" -> "false",
          "disabilityBenefit" -> "false",
          "severeDisabilityBenefit" -> "false",
          "carerAllowanceBenefit" -> "false",
          "noBenefit" -> "false"
        )).fold(
          errors =>
            errors.errors.head.message shouldBe Messages("cc.claimant.benefit.no.benefits.selected"),
          success =>
            success shouldBe true
        )
    }

    "throw a ValidationError when all benefit is selected (Parent)" in  {
      (new ClaimantBenefitsFormInstance).form.bind(
        Map(
          "incomeBenefit" -> "true",
          "disabilityBenefit" -> "true",
          "severeDisabilityBenefit" -> "true",
          "carerAllowanceBenefit" -> "true",
          "noBenefit" -> "true"
        )).fold(
          errors =>
            errors.errors.head.message shouldBe Messages("cc.claimant.benefit.invalid.parent.benefits.selected"),
          success =>
            success shouldBe true
        )
    }

    "pre-populate the form with a value" in {

      val claimantBenefitModel = ClaimantBenefitsPageModel(
        incomeBenefit = false,
        disabilityBenefit = false,
        severeDisabilityBenefit = false,
        carerAllowanceBenefit = false,
        noBenefit = true
      )
      val form = (new ClaimantBenefitsFormInstance).form.fill(claimantBenefitModel)
      form.get shouldBe claimantBenefitModel
    }

  }
}
