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
 * Created by adamconder on 04/02/2016.
 */
class HowManyChildrenFormSpec extends UnitSpec with FakeCCApplication with MockitoSugar {

  "HowManyChildrenForm" should {

    "accept a number between 1 and 19 inclusive" in {
      HowManyChildrenForm.form.bind(Map(
        "numberOfChildren" -> "1"
      )).fold(
          errors =>
            errors.errors shouldBe empty,
          success =>
            success shouldBe Some("1")
        )
    }

    "accept a number between 1 and 19 inclusive with spaces" in {
      HowManyChildrenForm.form.bind(Map(
        "numberOfChildren" -> " 19"
      )).fold(
          errors =>
            errors.errors.head.message should not be "error.number",
          success =>
            success shouldBe Some(" 19")
        )
    }

    "accept a number between 1 and 19 inclusive with 2 spaces" in {
      HowManyChildrenForm.form.bind(Map(
        "numberOfChildren" -> "  19"
      )).fold(
          errors =>
            errors.errors.head.message should not be "error.number",
          success =>
            success shouldBe Some("  19")
        )
    }

    "accept a number between 1 and 19 inclusive with leading zeros" in {
      HowManyChildrenForm.form.bind(Map(
        "numberOfChildren" -> "019"
      )).fold(
          errors =>
            errors.errors.head.message should not be "error.number",
          success =>
            success.get.toString shouldBe "019"
        )
    }

    "throw a ValidationError when providing a value less than 1" in {
      HowManyChildrenForm.form.bind(Map(
        "numberOfChildren" -> "-2"
      )).fold(
          errors =>
            errors.errors.head.message shouldBe "You must enter a number that’s not below 1 or above 19",
          success =>
            success should not be Some(-2)
        )
    }

    "throw a ValidationError when providing a number longer than two characters" in {
      HowManyChildrenForm.form.bind(Map(
        "numberOfChildren" -> "200"
      )).fold(
          errors =>
            errors.errors.head.message shouldBe "You must enter a number that’s not below 1 or above 19",
          success =>
            success should not be Some(200)
        )
    }

    "throw a ValidationError when not providing a whole number" in {
      HowManyChildrenForm.form.bind(Map(
        "numberOfChildren" -> "2.00"
      )).fold(
          errors =>
            errors.errors.head.message shouldBe "You must enter a number that’s not below 1 or above 19",
          success =>
            success should not be Some(2.00)
        )
    }

    "throw a ValidationError when providing a value greater than 19" in {
      HowManyChildrenForm.form.bind(Map(
        "numberOfChildren" -> "20"
      )).fold(
          errors =>
            errors.errors.head.message shouldBe "You must enter a number that’s not below 1 or above 19",
          success =>
            success should not be Some(20)
        )
    }

    "throw a ValidationError when providing a character" in {
      HowManyChildrenForm.form.bind(Map(
        "numberOfChildren" -> "adam"
      )).fold(
          errors =>
            errors.errors.head.message shouldBe "You must enter a number that’s not below 1 or above 19",
          success =>
            success should not be Some("adam")
        )
    }

    "throw a ValidationError when providing special characters" in {
      HowManyChildrenForm.form.bind(Map(
        "numberOfChildren" -> "%&&^@sd1"
      )).fold(
          errors =>
            errors.errors.head.message shouldBe "You must enter a number that’s not below 1 or above 19",
          success =>
            success should not be Some("%&&^@sd1")
        )
    }

    "throw a ValidationError when not filled" in {
      HowManyChildrenForm.form.bind(Map(
        "numberOfChildren" -> ""
      )).fold(
          errors =>
            errors.errors.head.message shouldBe "Tell us how many children you have",
          success =>
            success should not be Some("")
        )
    }

    "throw a ValidationError when empty characters" in {
      HowManyChildrenForm.form.bind(Map(
        "numberOfChildren" -> "  "
      )).fold(
          errors =>
            errors.errors.head.message shouldBe "You must enter a number that’s not below 1 or above 19",
          success =>
            success should not be Some("")
        )
    }

    "prepopulate the form with a value" in {
      val form = HowManyChildrenForm.form.fill(Some("1"))
      form.get shouldBe Some("1")
    }

  }

}
