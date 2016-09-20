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
import models.pages.ChildDobPageModel
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import org.scalatest.mock.MockitoSugar
import play.api.i18n.Messages
import uk.gov.hmrc.play.test.UnitSpec

/**
 * Created by adamconder on 05/02/2016.
 */
class ChildDobFormSpec extends UnitSpec with FakeCCApplication with MockitoSugar {

  "ChildDobForm" should {

    "accept a valid date of birth" in {
      val formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss")
      val dateOfBirth = LocalDate.parse("2015-04-14T00:00:00", formatter)

      val outputModel = ChildDobPageModel(dob = Some(dateOfBirth))
      ChildDobForm.form.bind(Map(
        "dateOfBirth.day" -> "14",
        "dateOfBirth.month" -> "04",
        "dateOfBirth.year" -> "2015"
      )).fold(
          errors =>
            errors.errors shouldBe empty,
          success =>
            success shouldBe outputModel
        )
    }


    "throw a ValidationError when empty date of birth is provided" in {

      ChildDobForm.form.bind(Map(
        "dateOfBirth.day" -> "",
        "dateOfBirth.month" -> "",
        "dateOfBirth.year" -> ""
      )).fold(
          errors =>
            errors.errors.head.message shouldBe Messages("cc.child.dob.mandatory"),
          success =>
            success shouldBe true
        )
    }

    "throw a ValidationError when birth year is empty for date of birth is provided" in {

      ChildDobForm.form.bind(Map(
        "dateOfBirth.day" -> "14",
        "dateOfBirth.month" -> "02",
        "dateOfBirth.year" -> ""
      )).fold(
          errors =>
            errors.errors.head.message shouldBe "error.invalid.date.format",
          success =>
            success shouldBe true
        )
    }

    "throw a ValidationError when special characters are entered for date of birth is provided" in {

      ChildDobForm.form.bind(Map(
        "dateOfBirth.day" -> "£$",
        "dateOfBirth.month" -> "04",
        "dateOfBirth.year" -> "2015"
      )).fold(
          errors =>
            errors.errors.head.message shouldBe "error.invalid.date.format",
          success =>
            success shouldBe true
        )
    }

    "throw a ValidationError when an incorrect date for date of birth is provided" in {

      ChildDobForm.form.bind(Map(
        "dateOfBirth.day" -> "41",
        "dateOfBirth.month" -> "04",
        "dateOfBirth.year" -> "2015"
      )).fold(
          errors =>
            errors.errors.head.message shouldBe "error.invalid.date.format",
          success =>
            success shouldBe true
        )
    }

    "throw a ValidationError when an incorrect month for date of birth is provided" in {

      ChildDobForm.form.bind(Map(
        "dateOfBirth.day" -> "24",
        "dateOfBirth.month" -> "18",
        "dateOfBirth.year" -> "2015"
      )).fold(
          errors =>
            errors.errors.head.message shouldBe "error.invalid.date.format",
          success =>
            success shouldBe true
        )
    }

    "throw a ValidationError when an incorrect year for date of birth is provided" in {

      ChildDobForm.form.bind(Map(
        "dateOfBirth.day" -> "24",
        "dateOfBirth.month" -> "18",
        "dateOfBirth.year" -> "0000"
      )).fold(
          errors =>
            errors.errors.head.message shouldBe "error.invalid.date.format",
          success =>
            success shouldBe true
        )
    }


    "pre-populate the form with a value" in {
      val formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss")
      val dateOfBirth = LocalDate.parse("2015-04-14T00:00:00", formatter)

      val childDetailModel = ChildDobPageModel(dob = Some(dateOfBirth))
      val form = ChildDobForm.form.fill(childDetailModel)
      form.get shouldBe childDetailModel
    }
  }
}
