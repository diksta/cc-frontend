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
import models.pages.HouseholdBenefitsPageModel
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.test.UnitSpec

/**
 * Created by user on 06/05/16.
 */
class HouseholdBenefitsFormSpec extends UnitSpec with FakeCCApplication with MockitoSugar {

  "HouseholdBenefitsForm" should {

    "throw a validation error when nothing is selected " in {

      HouseholdBenefitsForm.form.bind(Map(
        "benefits.tcBenefitSelection" -> "false",
        "benefits.ucBenefitSelection" -> "false",
        "benefits.noBenefitSelection" -> "false",
        "benefits.tcBenefitAmount" -> "",
        "benefits.ucBenefitAmount" -> ""

      )).fold(
        errors =>
          errors.errors.head.message shouldBe "You must select an answer",
        success =>
          success should not be Some(true)
      )
    }

    "throw a validation error when tc selection is made but no amount is entered in tcBenefit.amount " in {

      HouseholdBenefitsForm.form.bind(Map(
        "benefits.tcBenefitSelection" -> "true",
        "benefits.ucBenefitSelection" -> "false",
        "benefits.noBenefitSelection" -> "false",
        "benefits.tcBenefitAmount" -> "",
        "benefits.ucBenefitAmount" -> ""
      )).fold(
        errors =>
          errors.errors.head.message shouldBe "error.real",
        success =>
          success should not be Some(true)
      )
    }

    "throw a validation error when uc selection is made but no amount is entered in ucBenefit.amount " in {

      HouseholdBenefitsForm.form.bind(Map(
        "benefits.tcBenefitSelection" -> "false",
        "benefits.ucBenefitSelection" -> "true",
        "benefits.noBenefitSelection" -> "false",
        "benefits.tcBenefitAmount" -> "",
        "benefits.ucBenefitAmount" -> ""
      )).fold(
        errors =>
          errors.errors.head.message shouldBe "error.real",
        success =>
          success should not be Some(true)
      )
    }

    "throw a validation error when tc selection is made but 0 is entered in tcBenefit.amount " in {

      HouseholdBenefitsForm.form.bind(Map(
        "benefits.tcBenefitSelection" -> "true",
        "benefits.ucBenefitSelection" -> "false",
        "benefits.noBenefitSelection" -> "false",
        "benefits.tcBenefitAmount" -> "0",
        "benefits.ucBenefitAmount" -> ""
      )).fold(
        errors =>
          errors.errors.head.message shouldBe "You must enter a number between 1 and 9999.99",
        success =>
          success should not be Some(true)
      )
    }

    "throw a validation error when uc selection is made but 0 is entered in ucBenefit.amount " in {

      HouseholdBenefitsForm.form.bind(Map(
        "benefits.tcBenefitSelection" -> "false",
        "benefits.ucBenefitSelection" -> "true",
        "benefits.noBenefitSelection" -> "false",
        "benefits.tcBenefitAmount" -> "",
        "benefits.ucBenefitAmount" -> "0"
      )).fold(
        errors =>
          errors.errors.head.message shouldBe "You must enter a number between 1 and 9999.99",
        success =>
          success should not be Some(true)
      )
    }


    "throw a validation error when tc selection is made but non valid tc amount is entered -alphabets" in {

      HouseholdBenefitsForm.form.bind(Map(
        "benefits.tcBenefitSelection" -> "true",
        "benefits.ucBenefitSelection" -> "false",
        "benefits.noBenefitSelection" -> "false",
        "benefits.tcBenefitAmount" -> "addffff",
        "benefits.ucBenefitAmount" -> ""
      )).fold(
        errors =>
          errors.errors.head.message shouldBe "error.real",
        success =>
          success should not be Some(true)
      )
    }

    "throw a validation error when tc selection is made but non valid uc amount is entered -alphabets" in {

      HouseholdBenefitsForm.form.bind(Map(
        "benefits.tcBenefitSelection" -> "false",
        "benefits.ucBenefitSelection" -> "true",
        "benefits.noBenefitSelection" -> "false",
        "benefits.tcBenefitAmount" -> "",
        "benefits.ucBenefitAmount" -> "addffff"
      )).fold(
        errors =>
          errors.errors.head.message shouldBe "error.real",
        success =>
          success should not be Some(true)
      )
    }

    "throw a validation error when tc selection is made but non valid tc amount is entered - symbols" in {

      HouseholdBenefitsForm.form.bind(Map(
        "benefits.tcBenefitSelection" -> "true",
        "benefits.ucBenefitSelection" -> "false",
        "benefits.noBenefitSelection" -> "false",
        "benefits.tcBenefitAmount" -> "**&&&",
        "benefits.ucBenefitAmount" -> ""
      )).fold(
        errors =>
          errors.errors.head.message shouldBe "error.real",
        success =>
          success should not be Some(true)
      )
    }

    "throw a validation error when uc selection is made but non valid uc amount is entered - symbols" in {

      HouseholdBenefitsForm.form.bind(Map(
        "benefits.tcBenefitSelection" -> "false",
        "benefits.ucBenefitSelection" -> "true",
        "benefits.noBenefitSelection" -> "false",
        "benefits.tcBenefitAmount" -> "",
        "benefits.ucBenefitAmount" -> "**&&&"
      )).fold(
        errors =>
          errors.errors.head.message shouldBe "error.real",
        success =>
          success should not be Some(true)
      )
    }

    "throw a validation error when tc selection is made but negative number is entered in tcBenefit.amount " in {

      HouseholdBenefitsForm.form.bind(Map(
        "benefits.tcBenefitSelection" -> "true",
        "benefits.ucBenefitSelection" -> "false",
        "benefits.noBenefitSelection" -> "false",
        "benefits.tcBenefitAmount" -> "-500",
        "benefits.ucBenefitAmount" -> ""
      )).fold(
        errors =>
          errors.errors.head.message shouldBe "You must enter a number between 1 and 9999.99",
        success =>
          success should not be Some(true)
      )
    }

    "throw a validation error when uc selection is made but negative number is entered in ucBenefit.amount " in {

      HouseholdBenefitsForm.form.bind(Map(
        "benefits.tcBenefitSelection" -> "false",
        "benefits.ucBenefitSelection" -> "true",
        "benefits.noBenefitSelection" -> "false",
        "benefits.tcBenefitAmount" -> "",
        "benefits.ucBenefitAmount" -> "-500"
      )).fold(
        errors =>
          errors.errors.head.message shouldBe "You must enter a number between 1 and 9999.99",
        success =>
          success should not be Some(true)
      )
    }

    "throw a validation error when tc selection is made but tcBenefit.amount greater than 9999.99" in {

      HouseholdBenefitsForm.form.bind(Map(
        "benefits.tcBenefitSelection" -> "true",
        "benefits.ucBenefitSelection" -> "false",
        "benefits.noBenefitSelection" -> "false",
        "benefits.tcBenefitAmount" -> "99999",
        "benefits.ucBenefitAmount" -> ""
      )).fold(
        errors =>
          errors.errors.head.message shouldBe "You must enter a number between 1 and 9999.99",
        success =>
          success should not be Some(true)
      )
    }

    "throw a validation error when uc selection is made but ucBenefit.amount greater than 9999.99" in {

      HouseholdBenefitsForm.form.bind(Map(
        "benefits.tcBenefitSelection" -> "false",
        "benefits.ucBenefitSelection" -> "true",
        "benefits.noBenefitSelection" -> "false",
        "benefits.tcBenefitAmount" -> "",
        "benefits.ucBenefitAmount" -> "99999"
      )).fold(
        errors =>
          errors.errors.head.message shouldBe "You must enter a number between 1 and 9999.99",
        success =>
          success should not be Some(true)
      )
    }

    "throw a validation error when uc and tc both benefits are selected" in {

      HouseholdBenefitsForm.form.bind(Map(
        "benefits.tcBenefitSelection" -> "true",
        "benefits.ucBenefitSelection" -> "true",
        "benefits.noBenefitSelection" -> "false",
        "benefits.tcBenefitAmount" -> "500",
        "benefits.ucBenefitAmount" -> "1000"
      )).fold(
        errors =>
          errors.errors.head.message shouldBe "You can select only \"Tax credits\" or \"Universal Credit\" or \"I don't get either\"",
        success =>
          success should not be Some(true)
      )
    }

    "throw a validation error when uc and no benefit both are selected" in {

      HouseholdBenefitsForm.form.bind(Map(
        "benefits.tcBenefitSelection" -> "false",
        "benefits.ucBenefitSelection" -> "true",
        "benefits.noBenefitSelection" -> "true",
        "benefits.tcBenefitAmount" -> "",
        "benefits.ucBenefitAmount" -> "1000"
      )).fold(
        errors =>
          errors.errors.head.message shouldBe "You can select only \"Tax credits\" or \"Universal Credit\" or \"I don't get either\"",
        success =>
          success should not be Some(true)
      )
    }

    "throw a validation error when tc and no benefit both are selected" in {

      HouseholdBenefitsForm.form.bind(Map(
        "benefits.tcBenefitSelection" -> "true",
        "benefits.ucBenefitSelection" -> "false",
        "benefits.noBenefitSelection" -> "true",
        "benefits.tcBenefitAmount" -> "500",
        "benefits.ucBenefitAmount" -> ""
      )).fold(
        errors =>
          errors.errors.head.message shouldBe "You can select only \"Tax credits\" or \"Universal Credit\" or \"I don't get either\"",
        success =>
          success should not be Some(true)
      )
    }

    "accept valid selection and valid tcBenefit.amount " in {

      HouseholdBenefitsForm.form.bind(Map(
        "benefits.tcBenefitSelection" -> "true",
        "benefits.tcBenefitAmount" -> "500",
        "benefits.ucBenefitAmount" -> ""
      )).fold(
        errors =>
          errors.errors.head.message shouldBe empty,
        success =>
          success should not be Some(true)
      )
    }

    "accept valid selection and valid ucBenefit.amount " in {

      HouseholdBenefitsForm.form.bind(Map(
        "benefits.tcBenefitSelection" -> "false",
        "benefits.ucBenefitSelection" -> "true",
        "benefits.noBenefitSelection" -> "false",
        "benefits.tcBenefitAmount" -> "",
        "benefits.ucBenefitAmount" -> "1000"
      )).fold(
        errors =>
          errors.errors.head.message shouldBe empty,
        success =>
          success should not be Some(true)
      )
    }

    "accept valid selection - NoBenefit " in {

      HouseholdBenefitsForm.form.bind(Map(
        "benefits.tcBenefitSelection" -> "false",
        "benefits.ucBenefitSelection" -> "false",
        "benefits.noBenefitSelection" -> "true",
        "benefits.tcBenefitAmount" -> "",
        "benefits.ucBenefitAmount" -> ""
      )).fold(
        errors =>
          errors.errors.head.message shouldBe empty,
        success =>
          success should not be Some(true)
      )
    }


    "pre-populate the form with values" in {
      val houseHoldBenefitsPageModel = _root_.models.pages.HouseholdBenefitsPageModel(
        benefits = _root_.models.pages.BenefitsPageModel(
          tcBenefitSelection = true,
          ucBenefitSelection = false,
          noBenefitSelection = false,
          tcBenefitAmount = Some(400),
          ucBenefitAmount = None
        )
      )

      val form = HouseholdBenefitsForm.form.fill(houseHoldBenefitsPageModel)
      form.get shouldBe _root_.models.pages.HouseholdBenefitsPageModel(
        benefits = _root_.models.pages.BenefitsPageModel(
          tcBenefitSelection = true,
          ucBenefitSelection = false,
          noBenefitSelection = false,
          tcBenefitAmount = Some(400),
          ucBenefitAmount = None
        )
      )
    }
  }
}
