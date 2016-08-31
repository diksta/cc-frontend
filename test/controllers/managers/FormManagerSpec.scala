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

package controllers.managers

import controllers.FakeCCApplication
import controllers.manager.FormManager
import form.{ClaimantIncomeLastYearFormInstance, ClaimantIncomeLastYearForm}
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.test.UnitSpec

/**
 * Created by adamconder on 12/02/2016.
 */
class FormManagerSpec extends UnitSpec with FakeCCApplication with MockitoSugar with FormManager {

  import play.api.data.Form
  import play.api.data.Forms._

  val form = Form[Option[Int]](
    mapping(
      "numberOfChildren" -> optional(number)
    )((numberOfChildren : Option[Int]) => numberOfChildren)((numberOfChildren : Option[Int]) => Some(numberOfChildren)))

  "FormManager" when {

    "modifying errors" should {

      "override a play framework error message with a custom message" in {
        form.bind(Map(
          "numberOfChildren" -> "  "
        )).fold(
            formWithErrors => {
              val manager = formService.overrideFormError[Option[Int]](formWithErrors, key = "error.number", value = "override message")
              manager.errors.head.message shouldBe "override message"
            },
            form =>
              form should not be Some("  ")
          )
      }

      "override a play framework error message with a custom message - replacing multiple error messages- " in {
        val bind = Map(
          "employment.selection" -> "true",
          "employment.income" -> "-dddd",
          "other.selection" -> "true",
          "other.income" -> "tttt&&",
          "benefits.selection" -> "true",
          "benefits.income" -> "200.00"
        )
        (new ClaimantIncomeLastYearFormInstance).form.bind(bind).fold(
            formWithErrors => {
              val keyValueMap = Map("employment.income" -> "override employment income error message",
                "other.income" -> "override other income error message",
                "benefits.income" -> "override benefit income error message"
              )
              val manager = formService.overrideFormError[_root_.models.pages.income.ClaimantIncomeLastYearPageModel](form = formWithErrors, errorCode = "error.real", keyValue = keyValueMap)
               manager.errors.head.message shouldBe "override employment income error message"
              manager.errors.tail.head.message shouldBe "override other income error message"
            },
            form =>
              form should not be Some("  ")
          )
      }

      "not override a play framework error message with a custom message when the key does not exist" in {
        form.bind(Map(
          "numberOfChildren" -> "  "
        )).fold(
            formWithErrors => {
              val manager = formService.overrideFormError[Option[Int]](formWithErrors, key = "error.incorrect.key", value = "override message")
              manager.errors.head.message should not be "override message"
            },
            form =>
              form should not be Some("  ")
          )
      }

    }

    "modifying input" should {

      "remove a single leading zeros for a valid number" in {
        val value = "01"
        val result = formService.removeLeadingZero(input = value)
        result shouldBe "1"
      }

      "not remove multiple leading zeros for a valid number" in {
        val value = "00001"
        val result = formService.removeLeadingZero(input = value)
        result shouldBe "00001"
      }

      "not remove leading zeros for an invalid number" in {
        val value = "1"
        val result = formService.removeLeadingZero(input = value)
        result shouldBe "1"
      }

      "not remove zeros when they're at the end of the string" in {
        val value = "10"
        val result = formService.removeLeadingZero(input = value)
        result shouldBe "10"
      }

    }

  }

}
