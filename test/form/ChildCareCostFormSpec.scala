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
 * Created by user on 05/02/16.
 */
class ChildCareCostFormSpec  extends UnitSpec with FakeCCApplication with MockitoSugar {

  "ChildCareCostForm" should {

    "accept valid childcare cost 0" in {
      val childCareModel = models.pages.ChildCarePageModel(Some(0.00), Some(false))
      val form1 = ChildCareCostForm.form.bind(Map(
        "childCareCost" -> "0.00",
        "childEducation" -> "false"
      )).fold(
          errors =>
            errors.errors shouldBe empty,
          success =>
            success shouldBe childCareModel
        )
    }

    "accept valid childcare cost 9999.99" in {
      val childCareModel = models.pages.ChildCarePageModel(Some(9999.99), Some(true))
      val form1 = ChildCareCostForm.form.bind(Map(
        "childCareCost" -> "9999.99",
        "childEducation" -> "true"
      )).fold(
          errors =>
            errors.errors shouldBe empty,
          success =>
            success shouldBe childCareModel
        )
    }

    "throw validation error when providing characters" in {
      ChildCareCostForm.form.bind(Map(
        "childCareCost" -> "ben"
      )).fold(
          errors =>
            errors.errors.head.message shouldBe Messages("cc.childcare.cost.error.not.a.number"),
          success =>
            success should not be Some("ben")
        )
    }

    "throw validation error when providing special character(s)" in {
      ChildCareCostForm.form.bind(Map(
        "childCareCost" -> "$£!"
      )).fold(
          errors =>
            errors.errors.head.message shouldBe Messages("cc.childcare.cost.error.not.a.number"),
          success =>
            success should not be Some("$£!")
        )
    }

    "throw validation error when providing a value greater than 9999.99" in {
      ChildCareCostForm.form.bind(Map(
        "childCareCost" -> "10000.00"
      )).fold(
          errors =>
            errors.errors.head.message shouldBe Messages("cc.childcare.cost.error.not.a.number"),
          success =>
            success should not be Some(10000.00)
        )
    }

    "throw validation error when providing a value less than 0" in {
      ChildCareCostForm.form.bind(Map(
        "childCareCost" -> "-0.01"
      )).fold(
          errors =>
            errors.errors.head.message shouldBe Messages("cc.childcare.cost.error.not.a.number"),
          success =>
            success should not be Some(-0.01)
        )
    }

    "throw validation error when form not filled" in {
      ChildCareCostForm.form.bind(Map(
        "childCareCost" -> ""
      )).fold(
          errors =>
            errors.errors.head.message shouldBe Messages("cc.childcare.cost.error.required"),
          success =>
            success should not be Some("")
        )
    }

    "pre-populate the form with a value" in {
      val childCareModel = models.pages.ChildCarePageModel(Some(1000.00), Some(false))
      val form = ChildCareCostForm.form.fill(childCareModel)
      form.get shouldBe models.pages.ChildCarePageModel(Some(1000.00), Some(false))
    }

    "accept valid yes for education" in {
      val childCareModel = models.pages.ChildCarePageModel(Some(0.00), Some(true))
      val form = ChildCareCostForm.form.fill(childCareModel)
      form.get shouldBe models.pages.ChildCarePageModel(Some(00.00), Some(true))
    }

    "accept valid no for education" in {
      val childCareModel = models.pages.ChildCarePageModel(Some(0.00), Some(false))
      val form = ChildCareCostForm.form.fill(childCareModel)
      form.get shouldBe models.pages.ChildCarePageModel(Some(00.00), Some(false))
    }

    "throw validation error when radio button not selected" in {
      ChildCareCostForm.form.bind(Map(
        "childCareCost" -> "0.00",
        "childEducation" -> ""
      )).fold(
          errors =>
            errors.errors.head.message shouldBe Messages("cc.childcare.education.error.required"),
          success =>
            success.childEducation should not be Some(false)
        )
    }

  }
}
