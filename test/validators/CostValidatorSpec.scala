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

package validators

import controllers.FakeCCApplication
import form.validator.CostValidator
import formatter.CostFormatter
import org.scalatest.mock.MockitoSugar
import play.api.Logger
import play.api.data.Forms._
import play.api.data.{Form, Mapping}
import uk.gov.hmrc.play.test.UnitSpec

/**
 * Created by adamconder on 27/01/2016.
 */

object MockCostValidator extends CostValidator {

  import play.api.data.format.Formats._

  // override Option[Int, Int] and pass None
  private def costDecimal(precision: Int, scale: Int): Mapping[BigDecimal] = {
    Logger.debug(s"calling override method costDecimal")
    of[BigDecimal] as CostFormatter.costFormatter(None)
  }
  override def cost : Mapping[BigDecimal] = costDecimal(4, 2)
}

class CostValidatorSpec extends UnitSpec with FakeCCApplication with MockitoSugar {

  val form = Form[BigDecimal](
    mapping(
      "cost" -> CostValidator.cost
    )((cost : BigDecimal) => cost)((cost: BigDecimal) => Option(cost)))

  val noPrecisionForm = Form[BigDecimal](
    mapping(
      "cost" -> MockCostValidator.cost
    )((cost : BigDecimal) => cost)((cost: BigDecimal) => Option(cost)))

  "CostValidator" should {

    "accept a BigDecimal to 2 decimal places" in {
      form.bind(Map(
        "cost" -> "200.00"
      )).fold(
        errors =>
          errors.errors shouldBe empty,
        success =>
          success shouldBe BigDecimal(200.00)
        )
    }

    "accept a BigDecimal of the lowest allowed amount 0.05" in {
      form.bind(Map(
        "cost" -> "0.05"
      )).fold(
          errors =>
            errors.errors shouldBe empty,
          success =>
            success shouldBe BigDecimal(0.05)
        )
    }

    "accept a BigDecimal of the highest allowed amount 9999.99" in {
      form.bind(Map(
        "cost" -> "9999.99"
      )).fold(
          errors =>
            errors.errors shouldBe empty,
          success =>
            success shouldBe BigDecimal(9999.99)
        )
    }

    "throw a ValidationError when providing more than 2 decimal places" in {
      form.bind(Map(
        "cost" -> "200.00276362"
      )).fold(
          errors =>
            errors.errors.head.message shouldBe "Have only 2 numbers after the decimal point",
          success =>
            success should not be BigDecimal(200.00276362)
        )
    }

    "throw a ValidationError when providing an amount less than 0 pence" in {
      form.bind(Map(
        "cost" -> "-0.01"
      )).fold(
        errors =>
            errors.errors.head.message shouldBe "Only enter a number between '0' and '9999.99'.",
        success =>
          success should not be BigDecimal(0.00)
        )
    }

    "throw a ValidationError when providing an amount over 9999.99" in {
      form.bind(Map(
        "cost" -> "10000.00"
      )).fold(
          errors =>
            errors.errors.head.message shouldBe "Only enter a number between '0' and '9999.99'.",
          success =>
            success should not be BigDecimal(10000.00)
        )
    }

    "throw a ValidationError when providing no cost" in {
      form.bind(Map(
        "cost" -> ""
      )).fold(
        errors =>
            errors.errors.head.message shouldBe "Only enter a number between '0' and '9999.99'.",
        success =>
            success should not be BigDecimal(200.00)
        )
    }

    "throw a ValidationError when providing characters" in {
      form.bind(Map(
        "cost" -> "One Pound"
      )).fold(
        errors =>
          errors.errors.head.message shouldBe "Only enter a number between '0' and '9999.99'.",
        success => {
          success should not be BigDecimal(1.00)
          success should not be "One Pound"
        }
        )
    }

    "throw a ValidationError when not providing a precision scale" in {
      try {
        noPrecisionForm.bind(Map(
          "cost" -> null
        )).fold(
            errors =>
              errors.errors shouldBe null,
            success =>
              success should not be BigDecimal(200.00276362)
          )
      } catch {
        case e: Exception =>
          e shouldBe a[Exception]

      }

    }
  }

}
