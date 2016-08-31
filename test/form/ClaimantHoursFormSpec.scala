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
import play.api.i18n.Messages
import uk.gov.hmrc.play.test.UnitSpec

/**
 * Created by user on 04/03/16.
 */
class ClaimantHoursFormSpec extends UnitSpec with FakeCCApplication with MockitoSugar {

  "ClaimantHoursForm" should {

    "accept valid number of hours 0 when there is no income present" in {
      val claimantHoursModel = models.pages.ClaimantHoursPageModel(Some(0.0))
      val previousIncome = Some(models.claimant.Income())
      val currentIncome = Some(models.claimant.Income())
      new ClaimantHoursFormInstance(parent = true, previousIncome, currentIncome).form.bind(Map(
        "hours" -> "0.0"
      )).fold(
          errors =>
            errors.errors shouldBe empty,
          success =>
            success shouldBe claimantHoursModel
        )
    }

    "throw validation error when number of hours is 0 and there is income present" in {
      val claimantHoursModel = models.pages.ClaimantHoursPageModel(Some(0.0))
      val previousIncome = Some(models.claimant.Income(Some(20000)))
      val currentIncome = Some(models.claimant.Income(Some(20000)))
      new ClaimantHoursFormInstance(parent = true, previousIncome, currentIncome).form.bind(Map(
        "hours" -> "0.0"
      )).fold(
        errors =>
          errors.errors.head.message shouldBe Messages("cc.claimant.hours.employment.income.entered.must.have.hours"),
        success =>
          success shouldBe claimantHoursModel
      )
    }

    "accept 0 hours when last year income is something and current year income is 0" in {
      val claimantHoursModel = models.pages.ClaimantHoursPageModel(Some(0.0))
      val previousIncome = Some(models.claimant.Income(Some(20000)))
      val currentIncome = Some(models.claimant.Income(Some(0)))
      new ClaimantHoursFormInstance(parent = true, previousIncome, currentIncome).form.bind(Map(
        "hours" -> "0.0"
      )).fold(
        errors =>
          errors.errors shouldBe empty,
        success =>
          success shouldBe claimantHoursModel
      )
    }

    "throw validation error when current year income has no change (is empty) and previous income has some value" in {
      val claimantHoursModel = models.pages.ClaimantHoursPageModel(Some(0.0))
      val previousIncome = Some(models.claimant.Income(Some(20000)))
      val currentIncome = Some(models.claimant.Income())
      new ClaimantHoursFormInstance(parent = true, previousIncome, currentIncome).form.bind(Map(
        "hours" -> "0.0"
      )).fold(
        errors =>
          errors.errors.head.message shouldBe Messages("cc.claimant.hours.employment.income.entered.must.have.hours"),
        success =>
          success shouldBe claimantHoursModel
      )
    }

    "accept valid number of hours 99.5" in {
      val claimantHoursModel = models.pages.ClaimantHoursPageModel(Some(99.5))
      val previousIncome = Some(models.claimant.Income(Some(2000.00)))
      val currentIncome = Some(models.claimant.Income(Some(2000.00)))
      new ClaimantHoursFormInstance(parent = true, previousIncome, currentIncome).form.bind(Map(
        "hours" -> "99.5"
      )).fold(
          errors =>
            errors.errors shouldBe empty,
          success =>
            success shouldBe claimantHoursModel
        )
    }

    "throw validation error when hours greater than 99.5" in {
      val previousIncome = Some(models.claimant.Income(Some(2000.00)))
      val currentIncome = Some(models.claimant.Income(Some(2000.00)))
      new ClaimantHoursFormInstance(parent = true, previousIncome, currentIncome).form.bind(Map(
        "hours" -> "299"
      )).fold(
          errors =>
            errors.errors.head.message shouldBe Messages("cc.claimant.hours.incorrect"),
          success =>
            success should not be Some("299")
        )
    }

    "throw validation error when hours less than 0" in {
      val previousIncome = Some(models.claimant.Income(Some(2000.00)))
      val currentIncome = Some(models.claimant.Income(Some(2000.00)))
      new ClaimantHoursFormInstance(parent = true, previousIncome, currentIncome).form.bind(Map(
        "hours" -> "-11"
      )).fold(
          errors =>
            errors.errors.head.message shouldBe Messages("cc.claimant.hours.incorrect"),
          success =>
            success should not be Some("-11")
        )
    }

    "throw validation error when hours is empty (Parent)" in {
      val previousIncome = Some(models.claimant.Income(Some(2000.00)))
      val currentIncome = Some(models.claimant.Income(Some(2000.00)))
      new ClaimantHoursFormInstance(parent = true, previousIncome, currentIncome).form.bind(Map(
        "hours" -> ""
      )).fold(
          errors =>
            errors.errors.head.message shouldBe Messages("cc.claimant.hours.parent.empty"),
          success =>
            success should not be Some("")
        )
    }

    "throw validation error when hours is empty (Partner)" in {
      val previousIncome = Some(models.claimant.Income(Some(2000.00)))
      val currentIncome = Some(models.claimant.Income(Some(2000.00)))
      new ClaimantHoursFormInstance(parent = false, previousIncome, currentIncome).form.bind(Map(
        "hours" -> ""
      )).fold(
        errors =>
          errors.errors.head.message shouldBe Messages("cc.claimant.hours.partner.empty"),
        success =>
          success should not be Some("")
      )
    }

    "throw validation error when providing characters" in {
      val previousIncome = Some(models.claimant.Income(Some(2000.00)))
      val currentIncome = Some(models.claimant.Income(Some(2000.00)))
      new ClaimantHoursFormInstance(parent = true, previousIncome, currentIncome).form.bind(Map(
        "hours" -> "ben"
      )).fold(
          errors =>
            errors.errors.head.message shouldBe "error.real",
          success =>
            success should not be Some("ben")
        )
    }

    "throw validation error when providing special characters" in {
      val previousIncome = Some(models.claimant.Income(Some(2000.00)))
      val currentIncome = Some(models.claimant.Income(Some(2000.00)))
      new ClaimantHoursFormInstance(parent = true, previousIncome, currentIncome).form.bind(Map(
        "hours" -> "£$!"
      )).fold(
          errors =>
            errors.errors.head.message shouldBe "error.real",
          success =>
            success should not be Some("£$!")
        )
    }

    "pre-populate the form with a value" in {
      val claimantHoursModel = models.pages.ClaimantHoursPageModel(Some(37.5))
      val previousIncome = Some(models.claimant.Income(Some(2000.00)))
      val currentIncome = Some(models.claimant.Income(Some(2000.00)))
      val form = new ClaimantHoursFormInstance(parent = true, previousIncome, currentIncome).form.fill(claimantHoursModel)
      form.get shouldBe models.pages.ClaimantHoursPageModel(Some(37.5))
    }

    "throw validation error when more than one decimal place" in {
      val previousIncome = Some(models.claimant.Income(Some(2000.00)))
      val currentIncome = Some(models.claimant.Income(Some(2000.00)))
      new ClaimantHoursFormInstance(parent = true, previousIncome, currentIncome).form.bind(Map(
        "hours" -> "30.45"
      )).fold(
        errors =>
          errors.errors.head.message shouldBe Messages("cc.claimant.hours.error.one.decimal.place"),
        success =>
          success should not be Some("30.45")
      )
    }

  }
}
